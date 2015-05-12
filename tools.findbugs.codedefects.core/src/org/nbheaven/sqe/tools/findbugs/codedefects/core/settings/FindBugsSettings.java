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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.settings;

import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.config.UserPreferences;

import org.openide.ErrorManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Iterator;


/**
 *
 * @author Sven Reimers
 */
final public class FindBugsSettings {
    private static UserPreferences userPreferences;
    private static String SETTINGS_DIR = System.getProperty("netbeans.user") + File.separatorChar +
        "config" + File.separatorChar + "findbugs";
    private static String SETTINGS_FILE = "UserPreferences.findbugs";

    static public UserPreferences getUserPreferences() {
        if (null == userPreferences) {
            userPreferences = createUserPreferences();
        }

        return userPreferences;
    }

    private static UserPreferences createUserPreferences() {
        UserPreferences up = UserPreferences.createDefaultUserPreferences();
        File settingsDir = new File(SETTINGS_DIR);
        File settingsFile = new File(SETTINGS_DIR + File.separator + SETTINGS_FILE);

        if (!settingsDir.exists()) {
            settingsDir.mkdirs();
        }

        try {
            if (!settingsFile.exists()) {
                FileOutputStream fos = null;

                try {
                    if (settingsFile.createNewFile()) {
                        fos = new FileOutputStream(settingsFile);

                        presetPrefs(up);

                        up.write(fos);
                    } else {
                        // TODO: Add persistence failure handling
                    }
                } finally {
                    if (null != fos) {
                        fos.close();
                    }
                }
            } else {
                FileInputStream fis = null;

                try {
                    fis = new FileInputStream(settingsFile);
                    up.read(fis);
                } finally {
                    if (null != fis) {
                        fis.close();
                    }
                }
            }
        } catch (IOException ioe) {
            // TODO: nothing read what should be done???
            ErrorManager.getDefault().notify(ioe);
        }

        return up;
    }

    public static void save() {
        if (null != userPreferences) {
            try {
                FileOutputStream fos = null;

                try {
                    File settingsFile = new File(SETTINGS_DIR + File.separator + SETTINGS_FILE);
                    fos = new FileOutputStream(settingsFile);
                    getUserPreferences().write(fos);
                } finally {
                    if (null != fos) {
                        fos.close();
                    }
                }
            } catch (IOException ioe) {
                // TODO: nothing written what should be done???
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }

    static void presetPrefs(UserPreferences up) {
        for (Iterator<Plugin> pluginIterator = DetectorFactoryCollection.instance().pluginIterator();
                pluginIterator.hasNext();) {
            Plugin plugin = pluginIterator.next();

            // Disable all detectors from 3rdPartyPlugins by Default!!!!
            if (!"FindBugs project".equals(plugin.getProvider())) {
                for (DetectorFactory detectorFactory : plugin.getDetectorFactories()) {
                    up.enableDetector(detectorFactory, false);
                }
            }
        }
    }
}
