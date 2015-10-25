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
package org.nbheaven.sqe.core.utilities;

import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Florian Vogler
 */
public class SQEProjectSupport {

    private SQEProjectSupport() {
    }

    public static Project findProject(Node node) {
        if (null == node) {
            return null;
        }

        Project project = node.getLookup().lookup(Project.class);

        if (null == project) {
            DataObject dataObject = node.getLookup().lookup(DataObject.class);
            project = findProjectByDataObject(dataObject);
        }
        return project;
    }

    public static Project findProjectByDataObject(DataObject dataObject) {
        if (null == dataObject) {
            return null;
        }

        return findProjectByFileObject(dataObject.getPrimaryFile());
    }

    public static Project findProjectByFileObject(FileObject fileObject) {
        if (null == fileObject) {
            return null;
        }

        return FileOwnerQuery.getOwner(fileObject);
    }

    public static boolean isProjectPackage(final String packageName, final Project project) {
        String folderName = packageName;

        folderName = folderName.replaceAll("\\.", "/");

        // com/ndsatcom/Schnulli.java
        SourceGroup[] sgs = ProjectUtilities.getJavaSourceGroups(project);

        for (SourceGroup sg : sgs) {
            FileObject mayBeFileObject = sg.getRootFolder().getFileObject(folderName);

            if ((null != mayBeFileObject) && mayBeFileObject.isValid()) {
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    public static boolean isProjectClass(final String className, final Project project) {
        String javaFileName = className;

        if (-1 != javaFileName.indexOf('$')) {
            javaFileName = javaFileName.substring(0, javaFileName.indexOf('$'));
        }

        javaFileName = javaFileName.replaceAll("\\.", "/") + ".java";

        // com/ndsatcom/Schnulli.java
        SourceGroup[] sgs = ProjectUtilities.getJavaSourceGroups(project);

        for (SourceGroup sg : sgs) {
            FileObject mayBeFileObject = sg.getRootFolder().getFileObject(javaFileName);

            if ((null != mayBeFileObject) && mayBeFileObject.isValid()) {
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    public static Lookup createContextLookup(Project project) {
        return Lookups.singleton(new AbstractNode(Children.LEAF, Lookups.singleton(project)));
    }
}
