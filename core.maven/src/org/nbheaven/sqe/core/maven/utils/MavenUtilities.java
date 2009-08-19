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
package org.nbheaven.sqe.core.maven.utils;

import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.nbheaven.sqe.core.maven.spi.MavenPluginConfigurationImpl;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator;

/**
 *
 * @author sven
 */
public final class MavenUtilities {

    public static MavenPluginConfiguration getPluginConfiguration(Project project, String groupId, String artifactId) {
        MavenProject mavenProject = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        Xpp3Dom reportConfiguration = mavenProject.getReportConfiguration(groupId, artifactId, null);
        if (null != reportConfiguration) {
            ExpressionEvaluator eval = new NBPluginParameterExpressionEvaluator(mavenProject,
                    MavenSettingsSingleton.getInstance().createUserSettingsModel(),
                    new Properties());

            return new MavenPluginConfigurationImpl(reportConfiguration, eval);
        }
        return null;
    }

}
