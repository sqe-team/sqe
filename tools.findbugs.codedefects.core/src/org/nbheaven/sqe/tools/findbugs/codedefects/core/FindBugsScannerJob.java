/* Copyright 2005,2006 Sven Reimers, Florian Vogler
 *
 * This file is part of the Software Quality Environment Project.
 *
 * The Software Quality Environment Project is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 2 of the License, or (at your option) any later version.
 *
 * The Software Quality Environment Project is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nbheaven.sqe.tools.findbugs.codedefects.core;

import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FilterBugReporter;
import edu.umd.cs.findbugs.FindBugs;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.NoClassesFoundToAnalyzeException;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.config.UserPreferences;
import edu.umd.cs.findbugs.filter.Filter;
import java.io.IOException;
import org.nbheaven.sqe.codedefects.core.spi.SQECodedefectScanner;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettings;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.ErrorManager;

/**
 *
 * @author Sven Reimers
 */
public abstract class FindBugsScannerJob extends SQECodedefectScanner.Job {

    static {
        Installer.installPluginUpdater();
    }

    private edu.umd.cs.findbugs.Project findBugsProject;
    private FindBugsResult findBugsResult;
    private Project project;

    FindBugsScannerJob(Project project) {
        this.project = project;
    }

    @Override
    protected String getDisplayName() {
        return "FindBugs scanning " + ProjectUtils.getInformation(getProject()).getDisplayName();
    }

    @Override
    protected void preScan() {
        super.preScan();
    }

    @Override
    protected void scan() {
        getProgressHandle().progress("Setting up FindBugs Engine ");
        findBugsProject = createFindBugsProject();
        this.findBugsResult = new FindBugsResult(getProject());
        executeFindBugs();
    }

    final protected Project getProject() {
        return this.project;
    }

    protected FindBugsResult getResult() {
        return findBugsResult;
    }

    abstract protected edu.umd.cs.findbugs.Project createFindBugsProject();

    private void executeFindBugs() {
        NbFindBugsProgress progressCallback = new NbFindBugsProgress(getProject(), getProgressHandle());

        edu.umd.cs.findbugs.BugReporter textReporter = new NbBugReporter(this.findBugsResult,
                progressCallback);

        // XXX should this be configurable?
        textReporter.setPriorityThreshold(Priorities.NORMAL_PRIORITY);

        FindBugs2 engine = new FindBugs2();
        engine.setProject(findBugsProject);

        // Honor current UserPreferences (ensures plugins are loaded)
        UserPreferences prefs;
        FindBugsSettingsProvider findBugsSettingsProvider = getProject().getLookup().lookup(FindBugsSettingsProvider.class);

        if (null == findBugsSettingsProvider) {
            prefs = FindBugsSettings.getUserPreferences();
        } else {
            prefs = findBugsSettingsProvider.getFindBugsSettings();
            String includeFileName = findBugsSettingsProvider.getIncludeFilter();
            if (null != includeFileName) {
                try {
                    Filter filter = new Filter(includeFileName);
                    textReporter = new FilterBugReporter(textReporter, filter, true);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            String excludeFileName = findBugsSettingsProvider.getExcludeFilter();
            if (null != excludeFileName) {
                try {
                    Filter filter = new Filter(excludeFileName);
                    textReporter = new FilterBugReporter(textReporter, filter, false);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        }

        engine.setBugReporter(textReporter);

        engine.setUserPreferences(prefs);

        engine.setDetectorFactoryCollection(DetectorFactoryCollection.instance());

        engine.setProgressCallback(progressCallback);

        //inhibt deep scanning (especially for j2ee projects...)
        engine.setScanNestedArchives(false);

        // Set analysis feature settings
        // XXX should this be configurable?
        engine.setAnalysisFeatureSettings(FindBugs.DEFAULT_EFFORT);
        /* Probably better to just use a standard setting:
        List<AnalysisFeatureSetting> settings = new ArrayList<AnalysisFeatureSetting>();
        settings.add(new AnalysisFeatureSetting(
                AnalysisFeatures.TRACK_VALUE_NUMBERS_IN_NULL_POINTER_ANALYSIS,
                true));
        settings.add(new AnalysisFeatureSetting(
                AnalysisFeatures.ACCURATE_EXCEPTIONS, true));
        settings.add(new AnalysisFeatureSetting(
                AnalysisFeatures.CONSERVE_SPACE, false));
        settings.add(new AnalysisFeatureSetting(
                AnalysisFeatures.MODEL_INSTANCEOF, true));
        settings.add(new AnalysisFeatureSetting(
                AnalysisFeatures.SKIP_HUGE_METHODS, false));
        engine.setAnalysisFeatureSettings(settings.toArray(
                new AnalysisFeatureSetting[settings.size()]));
         */

        // Run the analysis!
        try {
            engine.execute();
        } catch (NoClassesFoundToAnalyzeException ncftae) {
            // TODO - do something interesting here
            // TODO - add something to the result...
            ncftae.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
            // TODO - do something interesting here
        } catch (InterruptedException iex) {
            iex.printStackTrace();
            // TODO - do something interesting here
        } finally {
            progressCallback.getProgressHandle().finish();
        }
    }
}
