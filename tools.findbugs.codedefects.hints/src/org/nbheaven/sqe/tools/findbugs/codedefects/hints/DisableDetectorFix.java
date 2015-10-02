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
package org.nbheaven.sqe.tools.findbugs.codedefects.hints;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbCollections;

/**
 *
 * @author Florian Vogler
 */
final class DisableDetectorFix implements Fix {
    private final BugInstance bugInstance;
    private final Project project;

    public DisableDetectorFix(BugInstance bugInstance, Project project) {
        this.bugInstance = bugInstance;
        this.project = project;
    }

    @Override
    public String getText() {
        return "Disable detector for \"" + bugInstance.getBugPattern().getShortDescription() + "\"";
    }

    @Override
    public ChangeInfo implement() throws Exception {
        FindBugsSettingsProvider settingsProvider = project.getLookup().lookup(FindBugsSettingsProvider.class);
        if (settingsProvider != null) {
            UserPreferences findBugsSettings = settingsProvider.getFindBugsSettings();
            for (DetectorFactory detectorFactory : NbCollections.iterable(DetectorFactoryCollection.instance().factoryIterator())) {
                if (detectorFactory.getReportedBugPatterns().contains(bugInstance.getBugPattern())) {
                    findBugsSettings.enableDetector(detectorFactory, false);
                }
            }
            settingsProvider.setFindBugsSettings(findBugsSettings);
            FindBugsSession qualitySession = SQECodedefectSupport.retrieveSession(project, FindBugsSession.class);
            FindBugsResult result = qualitySession.getResult();
            if (result != null) {
                result.removeAllBugInstancesForBugPattern(bugInstance.getBugPattern());
            }
        }
        return null;
    }
    
}
