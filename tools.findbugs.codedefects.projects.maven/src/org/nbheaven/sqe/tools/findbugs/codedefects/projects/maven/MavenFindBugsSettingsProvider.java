/* Copyright 2009 Milos Kleint
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
