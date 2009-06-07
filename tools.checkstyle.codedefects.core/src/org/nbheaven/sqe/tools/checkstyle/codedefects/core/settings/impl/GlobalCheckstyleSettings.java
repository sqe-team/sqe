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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettingsProvider;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Sven Reimers
 */
public class GlobalCheckstyleSettings extends AbstractCheckstyleSettings {

    public static final GlobalCheckstyleSettings INSTANCE = new GlobalCheckstyleSettings();

    private final Preferences modulePreferences;
    
    private FileObject checkstyleConfigFile;
    private FileObject propertiesFile;

    private GlobalCheckstyleSettings() {
        modulePreferences = NbPreferences.forModule(QualityProvider.class).parent().node("checkstyle");
        String configFile = modulePreferences.get("default_checkstyle_config_file", null);
        String absolutePath = System.getProperty("netbeans.user") + File.separatorChar + "config" +
                File.separatorChar + "Preferences" + File.separatorChar + modulePreferences.parent().absolutePath();
        if (null == configFile || null == FileUtil.toFileObject(new File(configFile))) {
            modulePreferences.put("default_checkstyle_config_file", absolutePath + File.separatorChar + "checkstyle.xml");
            try {
                modulePreferences.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
            File targetDirAsFile = new File(absolutePath);
            if (!targetDirAsFile.exists()){
                targetDirAsFile.mkdir();                
            }
            FileObject targetDir = FileUtil.toFileObject(targetDirAsFile);
            FileObject possibleCheckstyleConfigFile = targetDir.getFileObject("checkstyle.xml");
            if (null == possibleCheckstyleConfigFile  || !possibleCheckstyleConfigFile.isValid()) {
                try {
                    OutputStream os = new FileOutputStream(new File(FileUtil.toFile(targetDir), "checkstyle.xml"));
                    URL checkstyleDefaultURL = CheckstyleSettingsProvider.class.getResource("/org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/sun_checks.xml");
                    InputStream is = checkstyleDefaultURL.openStream();
                    FileUtil.copy(is, os);
                    checkstyleConfigFile = targetDir.getFileObject("checkstyle.xml");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } 
            } else {
                checkstyleConfigFile = possibleCheckstyleConfigFile;
            }
        } else {
            checkstyleConfigFile = FileUtil.toFileObject(new File(configFile));
        }
        String customPropertiesFilePath = absolutePath + File.separatorChar + "custom-checkstyle.properties";
        File file = new File(customPropertiesFilePath);
        if (file.exists()) {
            FileObject fo = FileUtil.toFileObject(file);
            try {
                getCustomProperties().load(fo.getInputStream());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public FileObject getCheckstyleConfigurationFile() {
        return checkstyleConfigFile;
    }

    public URL getCheckstyleConfigurationURL() {
        return null;
    }

    public FileObject getPropertiesFile() {
        return propertiesFile;
    }

    public void setCheckstyleConfigurationPath(String absolutePath) {
        checkstyleConfigFile = FileUtil.toFileObject(new File(absolutePath));
    }

    public void setPropertiesPath(String absolutePath) {
        if (absolutePath.equals("")) {
            propertiesFile = null;
        } else {
            propertiesFile = FileUtil.toFileObject(new File(absolutePath));
        }
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        if (null != propertiesFile) {
            try {
                properties.load(propertiesFile.getInputStream());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        properties.putAll(getCustomProperties());
        return properties;
    }

    public void setProperties(String properties) {
        InputStream is = null;
        try {
            getCustomProperties().clear();
            is = new ByteArrayInputStream(properties.getBytes("UTF-8"));
            try {
                getCustomProperties().load(is);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void flush() {
        modulePreferences.put("default_checkstyle_config_file", FileUtil.toFile(checkstyleConfigFile).getAbsolutePath());
        if (null != propertiesFile) {
            modulePreferences.put("default_checkstyle_properties_file", FileUtil.toFile(propertiesFile).getAbsolutePath());
        } else {
            modulePreferences.put("default_checkstyle_properties_file", "");
        }
        String absolutePath = System.getProperty("netbeans.user") + File.separatorChar + "config" +
                File.separatorChar + "Preferences" + File.separatorChar + modulePreferences.parent().absolutePath();
        String customPropertiesFilePath = absolutePath + File.separatorChar + "custom-checkstyle.properties";
        File file = new File(customPropertiesFilePath);
        try {
            if (!file.exists()) {
                    file.createNewFile();
            }
            FileObject fo = FileUtil.toFileObject(file);
            getCustomProperties().store(fo.getOutputStream(), "Custom Checkstyle Properties created via SQE");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
