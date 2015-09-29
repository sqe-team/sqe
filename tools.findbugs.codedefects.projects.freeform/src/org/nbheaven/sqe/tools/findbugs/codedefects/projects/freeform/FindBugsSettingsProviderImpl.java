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
package org.nbheaven.sqe.tools.findbugs.codedefects.projects.freeform;

import edu.umd.cs.findbugs.config.UserPreferences;
import java.util.prefs.Preferences;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.nbheaven.sqe.core.ant.AntUtilities;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettings;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author Sven Reimers
 */
@ProjectServiceProvider(service=FindBugsSettingsProvider.class, projectType={
    "org-netbeans-modules-apisupport-project",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"
}, projectTypes=@ProjectType(id="org-netbeans-modules-ant-freeform", position=0))
public class FindBugsSettingsProviderImpl extends FindBugsSettingsProvider {

    private static final String FINDBUGS_SETTINGS_FILE = "findbugs.settings.file";
    private static final String FINDBUGS_INCLUDE_FILTER = "findbugs.include.filter";
    private static final String FINDBUGS_EXCLUDE_FILTER = "findbugs.exclude.filter";
    
    private static final String FINDBUGS_SETTINGS_DEFAULT = "nbproject/findbugs.settings";
    private Project project;

    public FindBugsSettingsProviderImpl(Project project) {
        this.project = project;
    }

    private File getFindBugsSettingsFile() {
        String settingsFile = AntUtilities.evaluate(prefs().get(FINDBUGS_SETTINGS_FILE, FINDBUGS_SETTINGS_DEFAULT), project);
        return AntUtilities.resolveFile(settingsFile, project);
    }

    @Override
    public UserPreferences getFindBugsSettings() {
        File findBugsSettingsFile = getFindBugsSettingsFile();
        if (findBugsSettingsFile.isFile()) {
            UserPreferences prefs = UserPreferences.createDefaultUserPreferences();
            try {
                prefs.read(new FileInputStream(findBugsSettingsFile));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
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
        File findBugsSettingsFile = getFindBugsSettingsFile();
        findBugsSettingsFile.getParentFile().mkdirs();
        // XXX would be better to just write the diff from the default
        try {
            userPreferences.write(new FileOutputStream(findBugsSettingsFile));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public String getExcludeFilter() {
        String excludeFile = AntUtilities.evaluate(prefs().get(FINDBUGS_EXCLUDE_FILTER, null), project);

        // since this is not required just return null
        if (null == excludeFile) {
            return null;
        }

        return AntUtilities.resolveFile(excludeFile, project).getAbsolutePath();
    }

    @Override
    public String getIncludeFilter() {
        String includeFile = AntUtilities.evaluate(prefs().get(FINDBUGS_INCLUDE_FILTER, null), project);

        // since this is not required just return null
        if (null == includeFile) {
            return null;
        }

        return AntUtilities.resolveFile(includeFile, project).getAbsolutePath();
    }

    private Preferences prefs() {
        return ProjectUtils.getPreferences(project, FindBugsSettingsProviderImpl.class, true);
    }
    
}
