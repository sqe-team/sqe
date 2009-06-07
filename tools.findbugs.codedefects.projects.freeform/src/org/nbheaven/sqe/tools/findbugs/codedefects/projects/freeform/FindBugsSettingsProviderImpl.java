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

import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.nbheaven.sqe.core.ant.AntUtilities;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.support.ant.EditableProperties;

import org.openide.util.Exceptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettings;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
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
        EditableProperties sqeProperties = AntUtilities.getSQEProperties(project);
        String settingsFile = sqeProperties.get(FINDBUGS_SETTINGS_FILE);

        if (null == settingsFile) {
            settingsFile = FINDBUGS_SETTINGS_DEFAULT;

            sqeProperties.setProperty(FINDBUGS_SETTINGS_FILE, settingsFile);
            sqeProperties.setComment(FINDBUGS_SETTINGS_FILE,
                    new String[]{"#Path to FindbugsSettingsFile (relative)"}, true);
            AntUtilities.putSQEProperties(sqeProperties, project);
        }

        FileObject projectPropertiesFile = project.getProjectDirectory().getFileObject("nbproject/project.properties");
        // if availabe try substitution
        if (null != projectPropertiesFile) {
            PropertyProvider provider = PropertyUtils.propertiesFilePropertyProvider(FileUtil.toFile(projectPropertiesFile));
            PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.globalPropertyProvider(), provider);
            settingsFile = evaluator.evaluate(settingsFile);
        }

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
        EditableProperties sqeProperties = AntUtilities.getSQEProperties(project);
        String excludeFile = sqeProperties.get(FINDBUGS_EXCLUDE_FILTER);

        // since this is not required just return null
        if (null == excludeFile) {
            return null;
        }

        FileObject projectPropertiesFileObject = project.getProjectDirectory().getFileObject("nbproject/project.properties");
        File projectPropertiesFile = FileUtil.toFile(projectPropertiesFileObject);
        PropertyProvider provider = PropertyUtils.propertiesFilePropertyProvider(projectPropertiesFile);
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.globalPropertyProvider(), provider);
        excludeFile = evaluator.evaluate(excludeFile);
        File findBugsExcludeFilterFile = new File(excludeFile);
        if (!findBugsExcludeFilterFile.isAbsolute()) {
            FileObject findBugsExcludeFilterFileObject = project.getProjectDirectory().getFileObject("nbproject/" + excludeFile);
            findBugsExcludeFilterFile = FileUtil.toFile(findBugsExcludeFilterFileObject);
        }        
        return findBugsExcludeFilterFile.getAbsolutePath();
    }

    @Override
    public String getIncludeFilter() {
        EditableProperties sqeProperties = AntUtilities.getSQEProperties(project);
        String includeFile = sqeProperties.get(FINDBUGS_INCLUDE_FILTER);

        // since this is not required just return null
        if (null == includeFile) {
            return null;
        }

        FileObject projectPropertiesFileObject = project.getProjectDirectory().getFileObject("nbproject/project.properties");
        File projectPropertiesFile = FileUtil.toFile(projectPropertiesFileObject);
        PropertyProvider provider = PropertyUtils.propertiesFilePropertyProvider(projectPropertiesFile);
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.globalPropertyProvider(), provider);
        includeFile = evaluator.evaluate(includeFile);
        File findBugsIncludeFilterFile = new File(includeFile);
        if (!findBugsIncludeFilterFile.isAbsolute()) {
            FileObject findBugsIncludeFilterFileObject = project.getProjectDirectory().getFileObject("nbproject/" + includeFile);
            findBugsIncludeFilterFile = FileUtil.toFile(findBugsIncludeFilterFileObject);
        }        
        return findBugsIncludeFilterFile.getAbsolutePath();
    }
    
    
}
