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
package org.nbheaven.sqe.core.ant;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.Exceptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author sven
 */
public class AntUtilities {
    private static final String SQE_PROPERTY_FILE = "sqe.properties";

    /** Creates a new instance of AntUtilities */
    private AntUtilities() {
    }

    static File sqeProperties(final Project project) {
        File file = null;
        try {
            FileObject sqePropertyFileObject = project.getProjectDirectory().getFileObject("nbproject/" + SQE_PROPERTY_FILE);
            if (null == sqePropertyFileObject) {
                sqePropertyFileObject = project.getProjectDirectory().getFileObject("nbproject").createData(SQE_PROPERTY_FILE);
            }
            file  = FileUtil.toFile(sqePropertyFileObject);
            return file;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return file;
    }
   
    public static EditableProperties getSQEProperties(final Project project) {
        File sqeProperties = sqeProperties(project);
        EditableProperties editableProperties = new EditableProperties(true);
        try {
            FileInputStream is = new FileInputStream(sqeProperties);
            try {
                editableProperties.load(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            Logger.getLogger(PropertyUtils.class.getName()).log(Level.INFO, null, e);
        }
        return editableProperties;
    }
    
    
    public static void putSQEProperties(final EditableProperties editableProperties, final Project project) {
        File sqeProperties = sqeProperties(project);

        try {
            FileOutputStream os = new FileOutputStream(sqeProperties);

            try {
                editableProperties.store(os);
            } finally {
                os.close();
            }
        } catch (IOException e) {
            Logger.getLogger(PropertyUtils.class.getName()).log(Level.INFO, null, e);
        }
    }

    //    public static void putSQEProperties(final Project project, final EditableProperties properties) throws IOException {
    //        try {
    //            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
    //                public Void run() throws IOException {
    //                    FileObject bp = FileUtil.toFileObject(ubp);
    //                    if (bp == null) {
    //                        if (!ubp.exists()) {
    //                            ubp.getParentFile().mkdirs();
    //                            new FileOutputStream(ubp).close();
    //                            assert ubp.isFile() : "Did not actually make " + ubp;
    //                        }
    //                        bp = FileUtil.toFileObject(ubp);
    //                        if (bp == null) {
    //                            // XXX ugly (and will not correctly notify changes) but better than nothing:
    //                            ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - cannot properly write to " + ubp + "; might be because your user directory is on a Windows UNC path (issue #46813)? If so, try using mapped drive letters.");
    //                            OutputStream os = new FileOutputStream(ubp);
    //                            try {
    //                                properties.store(os);
    //                            } finally {
    //                                os.close();
    //                            }
    //                            return null;
    //                        }
    //                    }
    //                    FileLock lock = bp.lock();
    //                    try {
    //                        OutputStream os = bp.getOutputStream(lock);
    //                        try {
    //                            properties.store(os);
    //                        } finally {
    //                            os.close();
    //                        }
    //                    } finally {
    //                        lock.releaseLock();
    //                    }
    //                    return null;
    //                }
    //            });
    //        } catch (MutexException e) {
    //            throw (IOException)e.getException();
    //        }
    //    }
}
