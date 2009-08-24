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
package org.nbheaven.sqe.tools.checkstyle.codedefects.projects.freeform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;
import org.nbheaven.sqe.core.ant.AntUtilities;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettings;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettingsProvider;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl.AbstractCheckstyleSettings;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl.GlobalCheckstyleSettings;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
public class CheckstyleSettingsProviderImpl implements CheckstyleSettingsProvider {

    private static final String CHECKSTYLE_CONFIGURATION_FILE = "checkstyle.configuration.file";
    private static final String CHECKSTYLE_PROPERTIES_FILE = "checkstyle.configuration.properties";

    private static final String DEFAULT_CHECKSTYLE_CONFIGURATION_FILE = "checkstyle.xml";
    private static final String DEFAULT_CHECKSTYLE_PROPERTIES_FILE = "checkstyle.properties";

    final private Project project;

    CheckstyleSettingsProviderImpl(Project project) {
        this.project = project;
    }

    public CheckstyleSettings getCheckstyleSettings() {
        return new Settings(project);
    }

    public static class Settings extends AbstractCheckstyleSettings {

        private final Project project;

        private Settings(Project project) {
            this.project = project;
        }

        public FileObject getCheckstyleConfigurationFile() {
            String settingsFile = AntUtilities.evaluate(prefs().get(CHECKSTYLE_CONFIGURATION_FILE, DEFAULT_CHECKSTYLE_CONFIGURATION_FILE), project);

            File checkstyleSettingsFile = getCreateCheckstyleConfigurationFile(settingsFile);
            if (null == checkstyleSettingsFile) {
                checkstyleSettingsFile = getCreateCheckstyleConfigurationFile(DEFAULT_CHECKSTYLE_CONFIGURATION_FILE);
            }

            return FileUtil.toFileObject(checkstyleSettingsFile);
        }

        public URL getCheckstyleConfigurationURL() {
            return null;
        }

        public FileObject getPropertiesFile() {
            String propertiesFile = AntUtilities.evaluate(prefs().get(CHECKSTYLE_PROPERTIES_FILE, DEFAULT_CHECKSTYLE_PROPERTIES_FILE), project);

            File checkstylePropertiesFile = getCreateCheckstylePropertiesFile(propertiesFile);
            if (null == checkstylePropertiesFile) {
                checkstylePropertiesFile = getCreateCheckstylePropertiesFile(DEFAULT_CHECKSTYLE_PROPERTIES_FILE);
            }

            return FileUtil.toFileObject(checkstylePropertiesFile);
        }

        public Properties getProperties() {
            return System.getProperties();
        }

        private File getCreateCheckstyleConfigurationFile(String settingsFile) {

            File checkstyleConfigurationFile = new File(settingsFile);
            try {
                if (!checkstyleConfigurationFile.isAbsolute()) {
                    FileObject settingsFileObject = project.getProjectDirectory().getFileObject("nbproject/" + settingsFile);
                    if (null == settingsFileObject) {
                        FileObject targetDir = project.getProjectDirectory().getFileObject("nbproject");
                        FileObject copy = FileUtil.copyFile(GlobalCheckstyleSettings.INSTANCE.getCheckstyleConfigurationFile(), targetDir, settingsFile.substring(0, settingsFile.indexOf(".")));
                        return FileUtil.toFile(copy);
                    }
                    return FileUtil.toFile(settingsFileObject);
                }

                if (checkstyleConfigurationFile.exists()) {
                    return checkstyleConfigurationFile;
                } else {
                    FileObject targetDir = FileUtil.toFileObject(checkstyleConfigurationFile.getParentFile());
                    String targetFile = checkstyleConfigurationFile.getName();
                    FileObject copy = FileUtil.copyFile(GlobalCheckstyleSettings.INSTANCE.getCheckstyleConfigurationFile(), targetDir, targetFile);
                    return FileUtil.toFile(copy);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return checkstyleConfigurationFile;
        }

        private File getCreateCheckstylePropertiesFile(String settingsFile) {

            File checkstylePropertiesFile = new File(settingsFile);
            try {
                if (!checkstylePropertiesFile.isAbsolute()) {
                    FileObject settingsFileObject = project.getProjectDirectory().getFileObject("nbproject/" + settingsFile);
                    if (null == settingsFileObject) {
                        settingsFileObject = project.getProjectDirectory().getFileObject("nbproject").createData(settingsFile);
                    }
                    checkstylePropertiesFile = FileUtil.toFile(settingsFileObject);
                }

                return checkstylePropertiesFile;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return checkstylePropertiesFile;
        }

        
        public void setCheckstyleConfigurationPath(String configFilePath) {
            prefs().put(CHECKSTYLE_CONFIGURATION_FILE, configFilePath);
        }

        public void setPropertiesPath(String propertiesFilePath) {
            prefs().put(CHECKSTYLE_PROPERTIES_FILE, propertiesFilePath);
        }

        public void setProperties(String properties) {
            // TODO
        }

        private Preferences prefs() {
            return ProjectUtils.getPreferences(project, CheckstyleSettingsProviderImpl.class, true);
        }

    }

}
