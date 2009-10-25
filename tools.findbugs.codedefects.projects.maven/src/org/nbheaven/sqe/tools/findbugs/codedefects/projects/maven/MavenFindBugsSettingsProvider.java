/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbheaven.sqe.tools.findbugs.codedefects.projects.maven;

import edu.umd.cs.findbugs.config.UserPreferences;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettings;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class MavenFindBugsSettingsProvider extends FindBugsSettingsProvider {

    @Override
    public UserPreferences getFindBugsSettings() {
//        File findBugsSettingsFile = getFindBugsSettingsFile();
//        if (findBugsSettingsFile.isFile()) {
//            UserPreferences prefs = UserPreferences.createDefaultUserPreferences();
//            try {
//                prefs.
//                prefs.read(new FileInputStream(findBugsSettingsFile));
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//            return prefs;
//        } else {
            UserPreferences globalDefaultPreferences = FindBugsSettings.getUserPreferences();
            presetPreferences(globalDefaultPreferences);
            return globalDefaultPreferences;
//        }
    }

    @Override
    public void setFindBugsSettings(UserPreferences userPreferences) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

}
