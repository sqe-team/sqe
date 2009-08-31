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
package org.nbheaven.sqe.tools.checkstyle.codedefects.projects.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettings;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettingsProvider;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.nbheaven.sqe.core.maven.utils.MavenUtilities;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl.AbstractCheckstyleSettings;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl.GlobalCheckstyleSettings;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author sven
 */
@ProjectServiceProvider(service=CheckstyleSettingsProvider.class, projectType="org-netbeans-modules-maven")
public class CheckstyleSettingsProviderImpl implements CheckstyleSettingsProvider {

    private final Project p;

    public CheckstyleSettingsProviderImpl(Project p) {
        this.p = p;
    }

    public CheckstyleSettings getCheckstyleSettings() {
        MavenPluginConfiguration pluginConfiguration = MavenUtilities.getPluginConfiguration(p, "org.apache.maven.plugins", "maven-checkstyle-plugin");
        if (null != pluginConfiguration) {
            String configLocation = pluginConfiguration.getValue("configLocation");
            String configProperties = pluginConfiguration.getValue("propertyExpansion");
            Properties properties = new Properties();
            if (null != configProperties && configProperties.length() > 0) {
                String[] split = configProperties.split("=");
                properties.put(split[0], split[1]);
            }
            try {
                URL url = new URL(configLocation);
                return new CheckstyleSettingsImpl(url, properties);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            File file = new File(configLocation);
            // if the config location cannot be found just ignore it and use the default
            // TODO: better error handling here - pass it to session and result somehow for display to user
            //       something like a problem report for executing the tool
            return new CheckstyleSettingsImpl(FileUtil.toFileObject(file), properties);
        }
        return new CheckstyleSettingsImpl();
    }

    private static class CheckstyleSettingsImpl extends AbstractCheckstyleSettings {

        private final URL url;
        private final FileObject checkstyleConfigurationFile;
        private final Properties properties;

        private CheckstyleSettingsImpl() {
            this((FileObject)null, null);
        }

        private CheckstyleSettingsImpl(FileObject checkstyleConfigurationFile, Properties properties) {
            this.checkstyleConfigurationFile = 
                    null == checkstyleConfigurationFile ? GlobalCheckstyleSettings.INSTANCE.getCheckstyleConfigurationFile() : checkstyleConfigurationFile ;
            this.properties = null == properties ? System.getProperties() : properties;
            this.url = null;
        }

        private CheckstyleSettingsImpl(URL url, Properties properties) {
            this.url = url;
            this.properties = null == properties ? System.getProperties() : properties;
            this.checkstyleConfigurationFile = null;
        }

        public FileObject getCheckstyleConfigurationFile() {
            return checkstyleConfigurationFile;
        }

        public URL getCheckstyleConfigurationURL() {
            return url;
        }

        public Properties getProperties() {
            return properties;
        }

        public FileObject getPropertiesFile() {
            return null;
        }

    }
}
