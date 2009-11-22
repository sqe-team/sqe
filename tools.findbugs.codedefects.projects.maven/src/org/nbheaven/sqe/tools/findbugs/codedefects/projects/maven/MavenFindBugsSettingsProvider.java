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

import edu.umd.cs.findbugs.config.ProjectFilterSettings;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.nbheaven.sqe.core.maven.utils.MavenUtilities;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettings;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service = FindBugsSettingsProvider.class, projectType = "org-netbeans-modules-maven")
public class MavenFindBugsSettingsProvider extends FindBugsSettingsProvider {
    private final Project p;

    public MavenFindBugsSettingsProvider(Project prj) {
        this.p = prj;
    }

    @Override
    public UserPreferences getFindBugsSettings() {
        final MavenPluginConfiguration pluginConfiguration = MavenUtilities.getReportPluginConfiguration(p, "org.codehaus.mojo", "findbugs-maven-plugin");
        if (pluginConfiguration.isDefinedInProject()) {
            UserPreferences prefs = UserPreferences.createDefaultUserPreferences();
            ProjectFilterSettings pfs = ProjectFilterSettings.createDefault();
            prefs.setProjectFilterSettings(pfs);

            String threshold = pluginConfiguration.getValue("threshold");
            if (threshold != null) {
                //TODO guard valid values
                pfs.setMinPriority(threshold);
            }
            String effort = pluginConfiguration.getValue("effort");
            if (effort != null) {
                //TODO guard valid values
                prefs.setEffort(effort);
            }
            return prefs;
        } else {
            UserPreferences globalDefaultPreferences = FindBugsSettings.getUserPreferences();
            presetPreferences(globalDefaultPreferences);
            return globalDefaultPreferences;
        }
    }

    @Override
    public void setFindBugsSettings(UserPreferences userPreferences) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

}
