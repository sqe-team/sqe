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

import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;
import org.nbheaven.sqe.core.ant.AntUtilities;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettings;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettingsProvider;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl.AbstractCheckstyleSettings;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Sven Reimers
 */
@ProjectServiceProvider(service=CheckstyleSettingsProvider.class, projectType={
    "org-netbeans-modules-ant-freeform",
    "org-netbeans-modules-apisupport-project",
    "org-netbeans-modules-java-j2seproject"
//    "org-netbeans-modules-web-project"
})
public class CheckstyleSettingsProviderImpl implements CheckstyleSettingsProvider {

    private static final String CHECKSTYLE_CONFIGURATION_FILE = "checkstyle.configuration.file";
    private static final String CHECKSTYLE_PROPERTIES_FILE = "checkstyle.configuration.properties";

    private static final String DEFAULT_CHECKSTYLE_CONFIGURATION_FILE = "nbproject/checkstyle.xml";
    private static final String DEFAULT_CHECKSTYLE_PROPERTIES_FILE = "nbproject/checkstyle.properties";

    final private Project project;

    public CheckstyleSettingsProviderImpl(Project project) {
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
            // XXX this will just return null if not already defined... what will then happen?
            // use of FileObject in CheckstyleSettings is confusing and inconsistent with e.g. PMDSettings
            return FileUtil.toFileObject(AntUtilities.resolveFile(settingsFile, project));
        }

        public URL getCheckstyleConfigurationURL() {
            return null;
        }

        public FileObject getPropertiesFile() {
            String propertiesFile = AntUtilities.evaluate(prefs().get(CHECKSTYLE_PROPERTIES_FILE, DEFAULT_CHECKSTYLE_PROPERTIES_FILE), project);
            return FileUtil.toFileObject(AntUtilities.resolveFile(propertiesFile, project));
        }

        public Properties getProperties() {
            return System.getProperties();
        }

        public void setCheckstyleConfigurationPath(String configFilePath) {
            if (configFilePath.length() > 0) {
                prefs().put(CHECKSTYLE_CONFIGURATION_FILE, configFilePath);
            }
        }

        public void setPropertiesPath(String propertiesFilePath) {
            if (propertiesFilePath.length() > 0) {
                prefs().put(CHECKSTYLE_PROPERTIES_FILE, propertiesFilePath);
            }
        }

        public void setProperties(String properties) {
            // TODO
        }

        private Preferences prefs() {
            return ProjectUtils.getPreferences(project, CheckstyleSettingsProviderImpl.class, true);
        }

    }

}
