/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbheaven.sqe.tools.findbugs.codedefects.hints;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbCollections;

/**
 *
 * @author fvo
 */
final class DisableDetectorFix implements Fix {
    private final BugInstance bugInstance;
    private final Project project;

    public DisableDetectorFix(BugInstance bugInstance, Project project) {
        this.bugInstance = bugInstance;
        this.project = project;
    }

    public String getText() {
        return "Disable detector for \"" + bugInstance.getBugPattern().getShortDescription() + "\"";
    }

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
            FindBugsSession qualitySession = project.getLookup().lookup(FindBugsSession.class);
            FindBugsResult result = qualitySession.getResult();
            if (result != null) {
                result.removeAllBugInstancesForBugPattern(bugInstance.getBugPattern());
            }
        }
        return null;
    }
    
}
