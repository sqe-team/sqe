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
package org.nbheaven.sqe.core.java.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author Sven Reimers
 */
public final class ProjectUtilities {

    private ProjectUtilities() {
    }

    public static FileObject[] getJavaSourceRoots(Project project) {
        SourceGroup[] groups = getJavaSourceGroups(project);

        FileObject[] fileObjects = new FileObject[groups.length];
        int i = 0;
        for (SourceGroup g : groups) {
            fileObjects[i] = g.getRootFolder();
            i++;
        }
        return fileObjects;
    }

    public static SourceGroup[] getJavaSourceGroups(Project project) {
        Sources s = ProjectUtils.getSources(project);
        return s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }

    public static List<String> findBinaryRoots(Project project) {

        ArrayList<String> binaries = new ArrayList<String>();
        for (SourceGroup sg : getJavaSourceGroups(project)) {
            for (URL url : BinaryForSourceQuery.findBinaryRoots(sg.getRootFolder().toURL()).getRoots()) {
                File checkFile = FileUtil.archiveOrDirForURL(url);
                if (checkFile != null && checkFile.exists()) {
                    binaries.add(checkFile.getAbsolutePath());
                }
            }
        }
        binaries.trimToSize();
        return binaries;
    }

    public static boolean areJavaSourcePackages(Node[] nodes) {
        boolean enable = false;
        for (Node node : nodes) {
            enable = isJavaSourcePackage(node);
            if (enable) {
                break;
            }
        }
        return enable;
    }

    public static boolean isJavaSourcePackage(Node node) {

        DataFolder dataFolder = node.getLookup().lookup(DataFolder.class);

        if (null == dataFolder) {
            return false;
        }
        return isJavaSourcePackage(dataFolder);
    }

    private static boolean isJavaSourcePackage(DataObject dataObject) {
        if (null == dataObject || !(dataObject instanceof DataFolder)) {
            return false;
        }
        return isJavaSourcePackage(dataObject.getPrimaryFile());
    }

    private static boolean isJavaSourcePackage(FileObject fileObject) {
        if (null == fileObject) {
            return false;
        }

        Project project = FileOwnerQuery.getOwner(fileObject);
        if (null == project) {
            return false;
        }

        SourceGroup[] sourceGroups = getJavaSourceGroups(project);

        boolean isPackage = false;

//        // First check if default package
        for (SourceGroup sourceGroup : sourceGroups) {
            isPackage = sourceGroup.getRootFolder().equals(fileObject);
            if (isPackage) {
                break;
            }
        }

//        // then check if package folder
        if (!isPackage) {
            for (SourceGroup sourceGroup : sourceGroups) {
                // do not ask the sourceGroup since contains is just for in/exclude of sources
                isPackage = FileUtil.isParentOf(sourceGroup.getRootFolder(), fileObject);
                if (isPackage) {
                    break;
                }
            }
        }
        return isPackage;
    }
}
