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
package org.nbheaven.sqe.tools.pmd.codedefects.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.logging.Logger;
import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDException;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.SourceType;
import org.nbheaven.sqe.codedefects.core.spi.SQECodedefectScanner;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.impl.PMDSettingsImpl;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Sven Reimers
 */
abstract class PMDScannerJob extends SQECodedefectScanner.Job {

    private static final Logger LOGGER = Logger.getLogger(PMDScannerJob.class.getName());
    private final Project project;
//    private PMDSession session;
    private PMDResult pmdResult;
    private PMD pmd;
    private RuleSet rules;
    private RuleContext ruleContext;
    private Report report;
    private PMDSettings settings;

    PMDScannerJob(Project project) {
        this.project = project;
    }

    protected final String getDisplayName() {
        return "PMD scanning " + ProjectUtils.getInformation(getProject()).getName();
    }

    protected final Project getProject() {
        return project;
    }

    protected final PMDResult getPMDResult() {
        return pmdResult;
    }

    protected final PMDSettings getPMDSettings() {
        return settings;
    }

    private void init() {
        pmd = new PMD();

        ruleContext = new RuleContext();
        report = new Report();
        ruleContext.setReport(report);

        PMDSettingsProvider settingsProvider = getProject().getLookup().lookup(PMDSettingsProvider.class);
        if (null == settingsProvider) {
            settings = PMDSettingsImpl.globalSettings();
        } else {
            settings = settingsProvider.getPMDSettings();
        }
        rules = settings.getActiveRules();
    }

    private SourceType getSourceType(FileObject fo) {
        String sourceLevel = SourceLevelQuery.getSourceLevel(fo);

        if ("1.3".equals(sourceLevel)) {
            return SourceType.JAVA_13;
        }

        if ("1.4".equals(sourceLevel)) {
            return SourceType.JAVA_14;
        }

        if ("1.5".equals(sourceLevel)) {
            return SourceType.JAVA_15;
        }

        if ("1.6".equals(sourceLevel)) {
            return SourceType.JAVA_15;
        }

        return SourceType.JAVA_15;
    }

    @Override
    protected void scan() {
        getProgressHandle().progress("Setting up PMD");
        init();
        executePMD();
        this.pmdResult = new PMDResult(report);
    }

//    @Override
//    protected final void scan() {
//        getProgressHandle().progress("Setting up Checkstyle");
//        init();
//        CheckstyleResult internalResult = new CheckstyleResult(getProject());
//        checker.addListener(internalResult);
//        executeCheckstyle();
//        checkstyleResult = internalResult;
//    }
    protected abstract void executePMD();

    protected final void executePMD(Collection<FileObject> fullList) {

        getProgressHandle().switchToDeterminate(fullList.size());

        int i = 0;

        for (FileObject fo : fullList) {
            Reader reader = null;

            try {
                try {
                    reader = new BufferedReader(new InputStreamReader(
                            fo.getInputStream(), FileEncodingQuery.getEncoding(fo)));
                    ruleContext.setSourceCodeFilename(fo.getName());
                    pmd.setJavaVersion(getSourceType(fo));

                    getProgressHandle().progress(i++);
                    getProgressHandle().progress("Scanning " + fo.getName());

                    pmd.processFile(reader, rules, ruleContext);

                    getProgressHandle().progress("Looking for next file");
                } catch (PMDException ex) {
                    LOGGER.warning("Failure running PMD on " + fo.getName() + " caused by " + ex.getCause().getMessage());     //NOI18N
                } catch (FileNotFoundException ex) {
                    LOGGER.warning("File foe FileObject could not be found: " + fo.getName());     //NOI18N
                }
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException ioex) {
                    }
                }
            }
        }
    }
}
