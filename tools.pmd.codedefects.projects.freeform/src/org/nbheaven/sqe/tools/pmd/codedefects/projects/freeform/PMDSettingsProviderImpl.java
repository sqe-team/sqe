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
import java.io.IOException;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.nbheaven.sqe.core.ant.AntUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
public class PMDSettingsProviderImpl implements PMDSettingsProvider {

    private static final String PMD_SETTINGS_FILE = "pmd.settings.file";
    private static final String PMD_SETTINGS_DEFAULT = "pmd.settings";

    private Project project;

    public PMDSettingsProviderImpl(Project project) {
        this.project = project;
    }

    private File getPMDSettingsFile() {
        EditableProperties sqeProperties = AntUtilities.getSQEProperties(project);
        String settingsFile = sqeProperties.get(PMD_SETTINGS_FILE);

        if (null == settingsFile) {
            settingsFile = PMD_SETTINGS_DEFAULT;
        }

        FileObject projectPropertiesFile = project.getProjectDirectory().getFileObject("nbproject/project.properties");
        // if availabe try substitution
        if (null != projectPropertiesFile) {
            PropertyProvider provider = PropertyUtils.propertiesFilePropertyProvider(FileUtil.toFile(projectPropertiesFile));
            PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.globalPropertyProvider(), provider);
            settingsFile = evaluator.evaluate(settingsFile);
        }

        File pmdBugsSettingsFile = getCreatePMDBugsSettingsFile(settingsFile);
        if (null == pmdBugsSettingsFile) {
            pmdBugsSettingsFile = getCreatePMDBugsSettingsFile(PMD_SETTINGS_DEFAULT);
        }

        return pmdBugsSettingsFile;
    }

    private File getCreatePMDBugsSettingsFile(String settingsFile) {

        File pmdBugsSettingsFile = new File(settingsFile);
        try {
            if (!pmdBugsSettingsFile.isAbsolute()) {
                FileObject settingsFileObject = project.getProjectDirectory().getFileObject("nbproject/" + settingsFile);
                if (null == settingsFileObject) {
                    settingsFileObject = project.getProjectDirectory().getFileObject("nbproject").createData(settingsFile);
                }
                pmdBugsSettingsFile = FileUtil.toFile(settingsFileObject);
            }

            // getGlobalPreferences
//            UserPreferences globalDefaultPreferences = PMDSettings.getUserPreferences();
//            presetPreferences(globalDefaultPreferences);
//            // write globalPresets to project file
//            globalDefaultPreferences.write(new FileOutputStream(pmdBugsSettingsFile));
            return pmdBugsSettingsFile;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return pmdBugsSettingsFile;
    }


    public PMDSettings getPMDSettings() {
        File pmdSettingsFile = getPMDSettingsFile();
        return new PMDSettingsImpl(pmdSettingsFile);
    }

}
