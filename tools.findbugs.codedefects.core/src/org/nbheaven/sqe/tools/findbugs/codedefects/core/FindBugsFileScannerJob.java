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
package org.nbheaven.sqe.tools.findbugs.codedefects.core;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;
import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Sven Reimers
 */
public class FindBugsFileScannerJob extends FindBugsScannerJob {

    private static final Logger LOG = Logger.getLogger(FindBugsFileScannerJob.class.getName());


    FindBugsSession findBugsSession;
    FileObject[] fileObjects;

    FindBugsFileScannerJob(Project project, FileObject... fileObjects) {
        super(project);
        this.fileObjects = fileObjects;
        findBugsSession = project.getLookup().lookup(FindBugsSession.class);
    }

    protected edu.umd.cs.findbugs.Project createFindBugsProject() {
        edu.umd.cs.findbugs.Project fibuProject = new edu.umd.cs.findbugs.Project();

        for (FileObject fo: fileObjects) {
            if (fo.isValid()) {
                File f = FileUtil.toFile(fo);
                if (f != null) {
                    fibuProject.addFile(f.getAbsolutePath());
                }
            }
        }

        SourceGroup[] groups = ProjectUtilities.getJavaSourceGroups(getProject());

        for (SourceGroup g : groups) {
            FileObject fo = g.getRootFolder();
            // add source dir findbugs
            File f = FileUtil.toFile(fo);
            if (f != null) {
                fibuProject.addSourceDir(f.getAbsolutePath());
            }

            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);

            if (null != cp) {
                for (ClassPath.Entry entry : cp.entries()) {
                    URL url = entry.getURL();
                    File checkFile = FileUtil.archiveOrDirForURL(url);
                    if (checkFile != null && checkFile.exists()) {
                        fibuProject.addAuxClasspathEntry(checkFile.getAbsolutePath());
                    } else {
                        LOG.warning("Bad file on auxiliary classpath: " + checkFile);
                    }
                }
            }
        }

        return fibuProject;
    }

}
