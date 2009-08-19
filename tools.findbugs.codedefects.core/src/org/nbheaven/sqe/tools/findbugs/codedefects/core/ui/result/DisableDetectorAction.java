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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.ui.result;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.config.UserPreferences;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.netbeans.api.project.Project;

/**
 *
 * @author Sven Reimers
 */
class DisableDetectorAction extends AbstractAction {

    private final BugInstance bugInstance;
    private final Project project;

    DisableDetectorAction(BugInstance bugInstance, Project project) {
        this.bugInstance = bugInstance;
        this.project = project;
        bugInstance.getBugPattern();
        putValue(Action.NAME, "Disable Detector");
    }

    public void actionPerformed(ActionEvent e) {
        FindBugsSettingsProvider settingsProvider = project.getLookup().lookup(FindBugsSettingsProvider.class);
        if (null != settingsProvider) {
            UserPreferences findBugsSettings = settingsProvider.getFindBugsSettings();
            for (Iterator<DetectorFactory> factoryIterator = DetectorFactoryCollection.instance().factoryIterator(); factoryIterator.hasNext();) {
                DetectorFactory detectorFactory = factoryIterator.next();
                if (detectorFactory.getReportedBugPatterns().contains(bugInstance.getBugPattern())) {
                    findBugsSettings.enableDetector(detectorFactory, false);
                }
            }
            settingsProvider.setFindBugsSettings(findBugsSettings);
        }

        FindBugsSession qualitySession = project.getLookup().lookup(FindBugsSession.class);
        FindBugsResult result = qualitySession.getResult();
        result.removeAllBugInstancesForBugPattern(bugInstance.getBugPattern());
    }
}
