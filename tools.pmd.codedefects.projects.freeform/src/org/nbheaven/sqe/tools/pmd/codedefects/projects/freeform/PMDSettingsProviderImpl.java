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
package org.nbheaven.sqe.tools.pmd.codedefects.projects.freeform;

import java.io.File;
import java.util.prefs.Preferences;
import org.nbheaven.sqe.core.ant.AntUtilities;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDIncludes;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.impl.PMDSettingsImpl;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author Sven Reimers
 */
@ProjectServiceProvider(service=PMDSettingsProvider.class, projectType={
    "org-netbeans-modules-ant-freeform",
    "org-netbeans-modules-apisupport-project",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"
})
public class PMDSettingsProviderImpl implements PMDSettingsProvider {

    private static final String PMD_SETTINGS_FILE = "pmd.settings.file";
    private static final String PMD_SETTINGS_DEFAULT = "nbproject/pmd.settings";

    private Project project;

    public PMDSettingsProviderImpl(Project project) {
        this.project = project;
    }

    private File getPMDSettingsFile() {
        String settingsFile = AntUtilities.evaluate(prefs().get(PMD_SETTINGS_FILE, PMD_SETTINGS_DEFAULT), project);
        return AntUtilities.resolveFile(settingsFile, project);
    }

    @Override
    public PMDSettings getPMDSettings() {
        File pmdSettingsFile = getPMDSettingsFile();
        return new PMDSettingsImpl(pmdSettingsFile);
    }

    private Preferences prefs() {
        return ProjectUtils.getPreferences(project, PMDSettingsProviderImpl.class, true);
    }

    @Override
    public PMDIncludes getPMDIncludes() {
        //default behaviour
        return null;
    }

}
