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

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Sven Reimers
 */
public class AntUtilities {

    private AntUtilities() {}

    /**
     * Try to evaluate a string possibly containing Ant properties in the context of a project.
     * @param raw a string to evaluate (null permitted, may contain properties in the format {@code ${foo}})
     * @param a project which might have discoverable properties files (or might not)
     * @return a possibly evaluated string (null only if the input was)
     */
    public static String evaluate(String raw, Project project) {
        if (raw == null) {
            return null;
        }
        FileObject projectPropertiesFile = project.getProjectDirectory().getFileObject("nbproject/project.properties");
        if (projectPropertiesFile != null) {
            PropertyProvider provider = PropertyUtils.propertiesFilePropertyProvider(FileUtil.toFile(projectPropertiesFile));
            PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.globalPropertyProvider(), provider);
            String t = evaluator.evaluate(raw);
            if (t != null) {
                return t;
            }
        }
        return raw; // fallback
    }

    /**
     * Resolve a file path in the context of a project.
     * @param path a possibly relative path
     * @param project a disk project
     * @return the corresponding file (which may or may not exist)
     */
    public static File resolveFile(String path, Project project) {
        File d = FileUtil.toFile(project.getProjectDirectory());
        if (d == null) {
            throw new IllegalArgumentException("Project " + project + " does not exist on disk");
        }
        return PropertyUtils.resolveFile(d, path);
    }

}
