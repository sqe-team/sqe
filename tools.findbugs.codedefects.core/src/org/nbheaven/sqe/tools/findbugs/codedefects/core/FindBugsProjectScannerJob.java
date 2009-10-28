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
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nbheaven.sqe.core.java.utils.CompileOnSaveHelper;
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
public class FindBugsProjectScannerJob extends FindBugsScannerJob {

    private static final Logger LOG = Logger.getLogger(FindBugsProjectScannerJob.class.getName());

    private final FindBugsSession findBugsSession;

    FindBugsProjectScannerJob(Project project) {
        super(project);
        findBugsSession = project.getLookup().lookup(FindBugsSession.class);
    }


    @Override
    protected void postScan() {
        findBugsSession.setResult(getResult());
        super.postScan();
        findBugsSession.scanningDone();
    }

    protected edu.umd.cs.findbugs.Project createFindBugsProject() {
        edu.umd.cs.findbugs.Project fibuProject = new edu.umd.cs.findbugs.Project();

        for (SourceGroup g : ProjectUtilities.getJavaSourceGroups(getProject())) {
            FileObject fo = g.getRootFolder();
            // add source dir findbugs
            File f = FileUtil.toFile(fo);
            if (f != null) {
                LOG.log(Level.FINE, "addSourceDir: {0}", f);
                fibuProject.addSourceDir(f.getAbsolutePath());
            }

            try {
                URL url = CompileOnSaveHelper.forSourceRoot(fo).binaryRoot(false);
                if (url != null) {
                    File checkFile = FileUtil.archiveOrDirForURL(url);
                    if (checkFile == null) {
                        LOG.warning("Skipping inconvertible binary entry " + url);
                        continue;
                    }
                    if (!checkFile.exists()) {
                        LOG.warning("Skipping nonexistent binary entry " + checkFile);
                        continue;
                    }
                    LOG.log(Level.FINE, "addFile: {0}", checkFile);
                    fibuProject.addFile(checkFile.getAbsolutePath());
                }
            } catch (IOException x) {
                LOG.log(Level.INFO, null, x);
            }

            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
            if (null != cp) {
                for (ClassPath.Entry entry : cp.entries()) {
                    URL url = entry.getURL();
                    try {
                        url = CompileOnSaveHelper.forClassPathEntry(url).binaryRoot(false);
                    } catch (IOException x) {
                        LOG.log(Level.INFO, null, x);
                    }
                    File checkFile = FileUtil.archiveOrDirForURL(url);
                    if (checkFile == null) {
                        LOG.warning("Skipping inconvertible classpath entry " + url);
                        continue;
                    }
                    if (!checkFile.exists()) {
                        LOG.warning("Skipping nonexistent classpath entry " + checkFile);
                        continue;
                    }
                    LOG.log(Level.FINER, "addAuxClasspathEntry: {0}", checkFile);
                    fibuProject.addAuxClasspathEntry(checkFile.getAbsolutePath());
                }
            }
        }

        return fibuProject;
    }

}
