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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettings;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Sven Reimers
 */
public class FindBugsSettingsProviderImpl extends FindBugsSettingsProvider {

    private static final String FINDBUGS_SETTINGS_FILE = "findbugs.settings.file";
    private static final String FINDBUGS_INCLUDE_FILTER = "findbugs.include.filter";
    private static final String FINDBUGS_EXCLUDE_FILTER = "findbugs.exclude.filter";
    
    private static final String FINDBUGS_SETTINGS_DEFAULT = "findbugs.settings";
    private Project project;

    /** Creates a new instance of FindBugsSettingsProviderImpl */
    public FindBugsSettingsProviderImpl(Project project) {
        this.project = project;
    }

    private File getFindBugsSettingsFile() {
        String settingsFile = AntUtilities.evaluate(prefs().get(FINDBUGS_SETTINGS_FILE, FINDBUGS_SETTINGS_DEFAULT), project);

        File findBugsSettingsFile = getCreateFindBugsSettingsFile(settingsFile);
        if (null == findBugsSettingsFile) {
            findBugsSettingsFile = getCreateFindBugsSettingsFile(FINDBUGS_SETTINGS_DEFAULT);
        }

        return findBugsSettingsFile;
    }

    private File getCreateFindBugsSettingsFile(String settingsFile) {

        File findBugsSettingsFile = new File(settingsFile);
        try {
            if (!findBugsSettingsFile.isAbsolute()) {
                FileObject settingsFileObject = project.getProjectDirectory().getFileObject("nbproject/" + settingsFile);
                if (null == settingsFileObject) {
                    settingsFileObject = project.getProjectDirectory().getFileObject("nbproject").createData(settingsFile);
                }
                findBugsSettingsFile = FileUtil.toFile(settingsFileObject);
            }

            // getGlobalPreferences
            UserPreferences globalDefaultPreferences = FindBugsSettings.getUserPreferences();
            presetPreferences(globalDefaultPreferences);
            // write globalPresets to project file
            globalDefaultPreferences.write(new FileOutputStream(findBugsSettingsFile));
            return findBugsSettingsFile;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return findBugsSettingsFile;
    }

    public UserPreferences getFindBugsSettings() {
        UserPreferences prefs = UserPreferences.getUserPreferences();
        File findBugsSettingsFile = getFindBugsSettingsFile();

        try {
            prefs.read(new FileInputStream(findBugsSettingsFile));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return prefs;
    }

    public void setFindBugsSettings(UserPreferences userPreferences) {
        File findBugsSettingsFile = getFindBugsSettingsFile();

        try {
            userPreferences.write(new FileOutputStream(findBugsSettingsFile));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
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

        File findBugsExcludeFilterFile = new File(excludeFile);
        if (!findBugsExcludeFilterFile.isAbsolute()) {
            FileObject findBugsExcludeFilterFileObject = project.getProjectDirectory().getFileObject("nbproject/" + excludeFile);
            findBugsExcludeFilterFile = FileUtil.toFile(findBugsExcludeFilterFileObject);
        }        
        return findBugsExcludeFilterFile.getAbsolutePath();
    }

    @Override
    public String getIncludeFilter() {
        String includeFile = AntUtilities.evaluate(prefs().get(FINDBUGS_INCLUDE_FILTER, null), project);

        // since this is not required just return null
        if (null == includeFile) {
            return null;
        }

        File findBugsIncludeFilterFile = new File(includeFile);
        if (!findBugsIncludeFilterFile.isAbsolute()) {
            FileObject findBugsIncludeFilterFileObject = project.getProjectDirectory().getFileObject("nbproject/" + includeFile);
            findBugsIncludeFilterFile = FileUtil.toFile(findBugsIncludeFilterFileObject);
        }        
        return findBugsIncludeFilterFile.getAbsolutePath();
    }

    private Preferences prefs() {
        return ProjectUtils.getPreferences(project, FindBugsSettingsProviderImpl.class, true);
    }
    
}
