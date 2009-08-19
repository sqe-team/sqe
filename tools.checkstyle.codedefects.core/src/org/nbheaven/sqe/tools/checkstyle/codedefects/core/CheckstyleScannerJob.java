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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.nbheaven.sqe.codedefects.core.spi.SQECodedefectScanner;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettings;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettingsProvider;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl.GlobalCheckstyleSettings;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
abstract class CheckstyleScannerJob extends SQECodedefectScanner.Job {

    private final Project project;
    private Checker checker;
    private CheckstyleResult checkstyleResult;

    CheckstyleScannerJob(Project project) {
        this.project = project;
    }

    protected final String getDisplayName() {
        return "Checkstyle scanning " + ProjectUtils.getInformation(getProject()).getName();
    }

    protected final Project getProject() {
        return project;
    }

    protected final CheckstyleResult getCheckstyleResult() {
        return checkstyleResult;
    }

    private final void init() {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        InputStream istream = null;
        try {
            Thread.currentThread().setContextClassLoader(Checker.class.getClassLoader());
            checker = new Checker();
            FileObject checkStyleConfigFile = null;
            URL checkStyleConfigURL = null;
            Properties properties = System.getProperties();
            if (null != getProject().getLookup().lookup(CheckstyleSettingsProvider.class)) {
                CheckstyleSettings checkstyleSettings = getProject().getLookup().lookup(CheckstyleSettingsProvider.class).getCheckstyleSettings();
                checkStyleConfigFile = checkstyleSettings.getCheckstyleConfigurationFile();
                checkStyleConfigURL = checkstyleSettings.getCheckstyleConfigurationURL();
                properties = checkstyleSettings.getProperties();
            } else {
                checkStyleConfigFile = GlobalCheckstyleSettings.INSTANCE.getCheckstyleConfigurationFile();
            }
            if (null != checkStyleConfigFile) {
                istream = checkStyleConfigFile.getInputStream();
            } else if (null != checkStyleConfigURL) {
                istream = checkStyleConfigURL.openStream();
            }

            // compensate for bad configuration
            if (!properties.containsKey("checkstyle.cache.file")) {
                properties.put("checkstyle.cache.file",
                        System.getProperty("netbeans.user") + File.separatorChar +
                        "cache" + File.separatorChar + "checkstyle");
            }
            if (!properties.containsKey("checkstyle.header.file")) {
                properties.put("checkstyle.header.file", "");
            }

            try {
                Configuration config = ConfigurationLoader.loadConfiguration(istream,
                        new PropertiesExpander(properties), true);
                checker.setModuleClassLoader(Checker.class.getClassLoader());
                checker.configure(config);
            } catch (CheckstyleException ce) {
                // Fallback better exception handling necessary
                checkStyleConfigFile = GlobalCheckstyleSettings.INSTANCE.getCheckstyleConfigurationFile();
                istream = checkStyleConfigFile.getInputStream();
                properties = GlobalCheckstyleSettings.INSTANCE.getProperties();
                Configuration config = ConfigurationLoader.loadConfiguration(istream,
                        new PropertiesExpander(properties), true);
                checker.setModuleClassLoader(Checker.class.getClassLoader());
                checker.configure(config);
            }
        } catch (CheckstyleException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (null != istream) {
                try {
                    istream.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    @Override
    protected final void scan() {
        getProgressHandle().progress("Setting up Checkstyle");
        init();
        CheckstyleResult internalResult = new CheckstyleResult(getProject());
        checker.addListener(internalResult);
        executeCheckstyle();
        checkstyleResult = internalResult;
    }

    protected abstract void executeCheckstyle();

    protected final void executeCheckstyle(Collection<FileObject> fullList) {

        getProgressHandle().switchToDeterminate(fullList.size());
        List<File> files = new ArrayList<File>(1);

        int i = 0;

        for (FileObject fo : fullList) {
            Reader reader = null;

            try {
                try {
                    getProgressHandle().progress(i++);
                    getProgressHandle().progress("Scanning " + fo.getName());

                    files.add(new File(fo.getURL().toURI()));
                    checker.process(files);
                    files.clear();
                    getProgressHandle().progress("Looking for next file");
                } catch (URISyntaxException use) {
                    use.printStackTrace();
                } catch (FileStateInvalidException fsie) {
                    fsie.printStackTrace();
                }
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException ioex) {
                    }
                }
            }
        }
    }
}
