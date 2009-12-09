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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nbheaven.sqe.core.java.utils.CompileOnSaveHelper;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Sven Reimers
 */
class FindBugsFileScannerJob extends FindBugsScannerJob {

    private static final Logger LOG = Logger.getLogger(FindBugsFileScannerJob.class.getName());

    private final FileObject sourceFile;

    FindBugsFileScannerJob(Project project, FileObject sourceFile) {
        super(project);
        this.sourceFile = sourceFile;
    }

    protected edu.umd.cs.findbugs.Project createFindBugsProject() {
        edu.umd.cs.findbugs.Project fibuProject = new edu.umd.cs.findbugs.Project();

        ClassPath sourcePath = ClassPath.getClassPath(sourceFile, ClassPath.SOURCE);
        if (sourcePath == null) {
            return fibuProject;
        }
        for (FileObject sourceRoot : sourcePath.getRoots()) {
            File sourceRootF = FileUtil.toFile(sourceRoot);
            if (sourceRootF != null) {
                // XXX this does not seem to suffice to suppress "unread field" on a field used from another class
                LOG.log(Level.FINER, "addSourceDir: {0}", sourceRootF);
                fibuProject.addSourceDir(sourceRootF.getAbsolutePath());
            }
        }
        FileObject sourceRoot = sourcePath.findOwnerRoot(sourceFile);

        String binaryName = sourcePath.getResourceName(sourceFile, '/', false); // "org/foo/MyClass"
        try {
            URL binaryRootU = CompileOnSaveHelper.forSourceRoot(sourceRoot).binaryRoot(false);
            if (binaryRootU != null && binaryRootU.getProtocol().equals("file")) {
                File binaryRoot = new File(binaryRootU.toURI());
                File clazz = new File(binaryRoot, binaryName + ".class");
                if (clazz.isFile()) {
                    LOG.log(Level.FINE, "addFile: {0}", clazz);
                    fibuProject.addFile(clazz.getAbsolutePath());
                    // Also check for nested classes:
                    for (File kid : clazz.getParentFile().listFiles()) {
                        String n = kid.getName();
                        if (n.endsWith(".class") && n.startsWith(binaryName.replaceFirst(".+/", "") + "$")) {
                            LOG.log(Level.FINE, "addFile: {0}", kid);
                            fibuProject.addFile(kid.getAbsolutePath());
                        }
                    }
                }
            }

            ClassPath cp = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);
            if (cp == null) {
                return fibuProject;
            }
            for (ClassPath.Entry entry : cp.entries()) {
                URL url = CompileOnSaveHelper.forClassPathEntry(entry.getURL()).binaryRoot(false);
                File checkFile = FileUtil.archiveOrDirForURL(url);
                if (checkFile != null && checkFile.exists()) {
                    LOG.log(Level.FINER, "addAuxClasspathEntry: {0}", checkFile);
                    fibuProject.addAuxClasspathEntry(checkFile.getAbsolutePath());
                } else {
                    LOG.warning("Bad file on auxiliary classpath: " + checkFile);
                }
            }
        } catch (Exception x) {
            LOG.log(Level.INFO, null, x);
        }
        return fibuProject;
    }

}
