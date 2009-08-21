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
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;

/**
 * @author sven
 */
public class AntUtilities {

    private static final String SQE_PROPERTY_FILE = "sqe.properties";

    private AntUtilities() {}

    // XXX support for automatic projects etc. can come in 6.8 with ProjectUtils.getCacheDirectory

    public static EditableProperties getSQEProperties(final Project project) {
        EditableProperties editableProperties = new EditableProperties(true);
        FileObject sqePropertyFileObject = project.getProjectDirectory().getFileObject("nbproject/" + SQE_PROPERTY_FILE);
        if (sqePropertyFileObject != null) {
            try {
                InputStream is = sqePropertyFileObject.getInputStream();
                try {
                    editableProperties.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                Logger.getLogger(AntUtilities.class.getName()).log(Level.INFO, null, e);
            }
        }
        return editableProperties;
    }
    
    public static void putSQEProperties(final EditableProperties editableProperties, final Project project) {
        FileObject nbproject = project.getProjectDirectory().getFileObject("nbproject");
        if (nbproject == null) {
            // XXX warn?
            return;
        }
        try {
            FileObject sqePropertiesFileObject = FileUtil.createData(nbproject, SQE_PROPERTY_FILE);
            OutputStream os = sqePropertiesFileObject.getOutputStream();
            try {
                editableProperties.store(os);
            } finally {
                os.close();
            }
        } catch (IOException e) {
            Logger.getLogger(AntUtilities.class.getName()).log(Level.INFO, null, e);
        }
    }

}
