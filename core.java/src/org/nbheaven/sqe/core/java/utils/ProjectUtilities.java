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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Sven Reimers
 */
public final class ProjectUtilities {

    private ProjectUtilities() {
    }

    public static SourceLevelQuery getSourceLevelQuery(Project project) {
        SourceLevelQuery sourceLevelQuery = project.getLookup().lookup(SourceLevelQuery.class);
        if (null == sourceLevelQuery) {
            sourceLevelQuery = Lookup.getDefault().lookup(SourceLevelQuery.class);
        }
        return sourceLevelQuery;
    }

    public static FileObject[] getSourceRoots(Project project) {
        SourceGroup[] groups = getSourceGroups(project);

        FileObject[] fileObjects = new FileObject[groups.length];
        int i = 0;
        for (SourceGroup g : groups) {
            fileObjects[i] = g.getRootFolder();
            i++;
        }
        return fileObjects;
    }

    public static SourceGroup[] getSourceGroups(Project project) {
        Sources s = project.getLookup().lookup(Sources.class);
        return s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }

    public static BinaryForSourceQuery.Result[] getBinaries(Project project) {
        Sources s = project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        SourceGroup[] sg = getSourceGroups(project);

        BinaryForSourceQuery.Result[] results = new BinaryForSourceQuery.Result[sg.length];
        int i = 0;
        for (SourceGroup g : s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            try {
                results[i] = BinaryForSourceQuery.findBinaryRoots(g.getRootFolder().getURL());
            } catch (FileStateInvalidException fsie) {
                ErrorManager.getDefault().notify(fsie);
            }
            i++;
        }
        return results;
    }

    public static List<String> findBinaryRoots(Project project) {

        ArrayList<String> binaries = new ArrayList<String>();
        try {
            for (BinaryForSourceQuery.Result result : getBinaries(project)) {
                for (URL url : result.getRoots()) {
                    String file = url.getFile();
                    if ("jar".equals(url.getProtocol())) {
                        file = new URL(file).getFile();
                    }
                    // ensure this is valid for FindBugs (remove trailing !/
                    String fixedUrl = file.replace("!/", "");
                    File checkFile = new File(URLDecoder.decode(fixedUrl, "UTF-8"));
                    if (checkFile.exists()) {
                        binaries.add(URLDecoder.decode(fixedUrl, "UTF-8"));
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException mue) {
            ErrorManager.getDefault().notify(mue);
        }
        binaries.trimToSize();
        return binaries;
    }

    @Deprecated
    public static boolean isJavaProject(Project project) {
        if (null == project) {
            return false;
        }

        Sources s = project.getLookup().lookup(Sources.class);

        if (null == s) {
            return false;
        }

        SourceGroup[] sg = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        if (sg.length == 0) {
            return false;
        }

        return true;
    }

    public static boolean areSourcePackages(Node[] nodes) {
        boolean enable = false;
        for (Node node : nodes) {
            enable = isSourcePackage(node);
            if (enable) {
                break;
            }
        }
        return enable;
    }

    public static boolean isSourcePackage(Node node) {

        DataFolder dataFolder = node.getLookup().lookup(DataFolder.class);

        if (null == dataFolder) {
            return false;
        }
        return isSourcePackage(dataFolder);
    }

    private static boolean isSourcePackage(DataObject dataObject) {
        if (null == dataObject || !(dataObject instanceof DataFolder)) {
            return false;
        }
        return isSourcePackage(dataObject.getPrimaryFile());
    }

    private static boolean isSourcePackage(FileObject fileObject) {
        if (null == fileObject) {
            return false;
        }

        Project project = FileOwnerQuery.getOwner(fileObject);
        if (null == project) {
            return false;
        }

        Sources sources = project.getLookup().lookup(Sources.class);
        if (null == sources) {
            return false;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

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
