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
import java.util.List;
import java.util.Properties;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettings;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettingsProvider;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.nbheaven.sqe.core.maven.utils.FileUtilities;
import org.nbheaven.sqe.core.maven.utils.MavenUtilities;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl.AbstractCheckstyleSettings;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl.GlobalCheckstyleSettings;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Sven Reimers
 */
@ProjectServiceProvider(service=CheckstyleSettingsProvider.class, projectType="org-netbeans-modules-maven")
public class CheckstyleSettingsProviderImpl implements CheckstyleSettingsProvider {

    private final Project p;

    public CheckstyleSettingsProviderImpl(Project p) {
        this.p = p;
    }

    @Override
    public CheckstyleSettings getCheckstyleSettings() {
        MavenPluginConfiguration pluginConfiguration = MavenUtilities.getReportPluginConfiguration(p, "org.apache.maven.plugins", "maven-checkstyle-plugin");
        if (pluginConfiguration.isDefinedInProject()) {
            String configLocation = pluginConfiguration.getValue("configLocation");
            if (configLocation == null) {
                configLocation = "config/sun_checks.xml"; //defaultvalue
            }
            String configProperties = pluginConfiguration.getValue("propertyExpansion");
            Properties properties = new Properties();
            if (null != configProperties && configProperties.length() > 0) {
                String[] split = configProperties.split("=");
                properties.put(split[0], split[1]);
            }
            //check is file is physically present in the project
            File file = FileUtilities.resolveFilePath(FileUtil.toFile(p.getProjectDirectory()), configLocation);
            if (file != null && file.exists()) {
                return new CheckstyleSettingsImpl(FileUtil.toFileObject(file), properties);
            }
            //check the default configurations present in the maven-checkstyle-plugin
            //check is file is present in the project's checkstyle plugin classpath
            List<File> deps = MavenUtilities.findDependencyArtifacts(p, "org.apache.maven.plugins", "maven-checkstyle-plugin", true);
            if (deps.size() > 0) {
                FileObject fo = null;
                for (File d : deps) {
                    FileObject fileFO = FileUtil.toFileObject(d);
                    if (FileUtil.isArchiveFile(fileFO)) {
                        FileObject root = FileUtil.getArchiveRoot(fileFO);
                        if (root != null) {
                            fo = root.getFileObject(configLocation);
                            if (fo != null) break;
                        }
                    }
                }
                if (fo != null) {
                    return new CheckstyleSettingsImpl(fo, properties);
                }
            }

            try {
                URL url = new URL(configLocation);
                return new CheckstyleSettingsImpl(url, properties);
            } catch (MalformedURLException ex) {
                // OK, not a URL (no protocol)
            }
                // TODO: better error handling here - pass it to session and result somehow for display to user
                //       something like a problem report for executing the tool
        }
        return null;
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

        @Override
        public FileObject getCheckstyleConfigurationFile() {
            return checkstyleConfigurationFile;
        }

        @Override
        public URL getCheckstyleConfigurationURL() {
            return url;
        }

        @Override
        public Properties getProperties() {
            return properties;
        }

        @Override
        public FileObject getPropertiesFile() {
            return null;
        }

    }
}
