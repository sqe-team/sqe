/* Copyright 2009 Jesse Glick.
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
 * along with SQE. If not, see <http://www.gnu.org/licenses/>.
 */

package org.nbheaven.sqe.core.java.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result2;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

// XXX FileUtil.addRecursiveListener cannot be used to listen for changes (java.source does not use FileObject?);
//     would allow FB to be rerun automatically after project changes (not necessarily desirable)

/**
 * Alternative to {@link BuildArtifactMapper} that lets you work with up-to-date bytecode without {@link JavaRunner}.
 * If a project has "Compile on Save" selected, <em>and</em> it is currently in automatic synchronization mode,
 * this class is mostly unnecessary because {@code BuildArtifactMapper} will copy classes to its regular
 * build directory. In other cases, there is currently no other way to get access to the IDE's internal bytecode cache.
 * @see <a href="https://netbeans.org/bugzilla/show_bug.cgi?id=178336">Proposed API</a>
 */
public final class CompileOnSaveHelper {
    
    private static final Logger LOG = Logger.getLogger(CompileOnSaveHelper.class.getName());

    private static final Method/* URL sources, true -> File|null */ getClassFolder;
    private static final Object taskCache;
    private static final Method/* FileObject sources, true -> boolean */ isInError;
    static {
        ClassLoader loader = JavaSource.class.getClassLoader();
        Method _getClassFolder = null;
        Object _taskCache = null;
        Method _isInError = null;
        try {
            _getClassFolder = Class.forName("org.netbeans.modules.java.source.indexing.JavaIndex", true, loader).
                    getMethod("getClassFolder", URL.class, boolean.class);
            Class<?> taskCacheClass;
            try {
                taskCacheClass = Class.forName("org.netbeans.modules.parsing.impl.indexing.errors.TaskCache", true, loader);
            } catch (ClassNotFoundException x) {
                taskCacheClass = Class.forName("org.netbeans.modules.java.source.tasklist.TaskCache", true, loader);
            }
            _taskCache = taskCacheClass.getMethod("getDefault").invoke(null);
            _isInError = taskCacheClass.getMethod("isInError", FileObject.class, boolean.class);
        } catch (Exception x) {
            LOG.log(Level.INFO, null, x);
        }
        getClassFolder = _getClassFolder;
        taskCache = _taskCache;
        isInError = _isInError;
    }

    private final FileObject sources;
    private final URL publicBinaries;
    private CompileOnSaveHelper(FileObject sources, URL publicBinaries) {
        assert sources != null || publicBinaries != null;
        this.sources = sources;
        this.publicBinaries = publicBinaries;
        LOG.log(Level.FINE, "created from {0} and {1}", new Object[] {sources, publicBinaries});
    }

    public static CompileOnSaveHelper forSourceRoot(FileObject root) {
        Parameters.notNull("root", root);
        URL[] binaryRoots = BinaryForSourceQuery.findBinaryRoots(root.toURL()).getRoots();
        return new CompileOnSaveHelper(root, binaryRoots.length > 0 ? binaryRoots[0] : null);
    }

    public static CompileOnSaveHelper forClassPathEntry(URL entry) {
        Parameters.notNull("entry", entry);
        Result2 result = SourceForBinaryQuery.findSourceRoots2(entry);
        FileObject[] roots = result.getRoots();
        FileObject sources = result.preferSources() && roots.length > 0 ? roots[0] : null;
        return new CompileOnSaveHelper(sources, entry);
    }

    /**
     * Gets a root of Java bytecode.
     * <p>If {@link #forSourceRoot} was used, or {@link #forClassPathEntry} was used
     * but the Java indexer is requested to scan a matching source root,
     * then this will be a copy of the current class cache, in an unspecified location.
     * The cache should be up to date in case all modified files have been saved and scanning has completed.
     * Otherwise the original classpath entry will be returned unchanged.
     * <p>Currently non-Java resources (e.g. {@code *.properties}) are <strong>not copied</strong> to the output.
     * <p>Generally this should be called within a Java source task at {@link org.netbeans.api.java.source.JavaSource.Phase#UP_TO_DATE}.
     * @param tolerateErrors if true, return cache dir even if known to contain errors, else return normal compiled binaries (if any)
     * @return a binary root, or null if {@link #forSourceRoot} was used and there is no known binary root,
     *         or if for some reason there is no class cache for this source root
     * @throws IOException if there were problems copying the class cache
     */
    public URL binaryRoot(boolean tolerateErrors) throws IOException {
        if (sources == null) {
            return publicBinaries;
        }
        if (!tolerateErrors && taskCache != null && isInError != null) {
            try {
                if ((Boolean) isInError.invoke(taskCache, sources, true)) {
                    LOG.log(Level.FINE, "skipping {0} since it is in error", sources);
                    return publicBinaries;
                }
            } catch (Exception x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        if (publicBinaries != null && publicBinaries.getProtocol().equals("file")) {
            try {
                File tag = new File(new File(publicBinaries.toURI()), ".netbeans_automatic_build");
                if (tag.isFile()) {
                    LOG.log(Level.FINE, "found tag {0}", tag);
                    return publicBinaries;
                }
            } catch (URISyntaxException x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        if (getClassFolder == null) {
            return publicBinaries;
        }
        URL sourcesURL = sources.toURL();
        File sigDir;
        try {
            sigDir = (File) getClassFolder.invoke(null, sourcesURL, true);
        } catch (Exception x) {
            throw (IOException) new IOException(x.toString()).initCause(x);
        }
        if (sigDir == null) {
            LOG.log(Level.FINE, "no sigdir for {0}", sourcesURL);
            return publicBinaries;
        }
        CRC32 crc = new CRC32();
        crc.update(sourcesURL.toString().getBytes("UTF-8"));
        String key = String.format("%08X", crc.getValue());
        File classDir = new File(getCacheDir(), key);
        LOG.log(Level.FINE, "synchronizing {0} to {1}", new Object[] {sigDir, classDir});
        copySigToClass(sigDir, classDir);
        return classDir.toURI().toURL();
    }

    private File getCacheDir() throws IOException {
        File tmp = null;
        if (sources != null) {
            Project owner = FileOwnerQuery.getOwner(sources);
            if (owner != null) {
                tmp = FileUtil.toFile(ProjectUtils.getCacheDirectory(owner, CompileOnSaveHelper.class));
            }
        }
        if (tmp == null) {
            // XXX use Places.getCacheSubdirectory("CompileOnSaveHelper") in NB 7.1
            tmp = new File(System.getProperty("netbeans.user"), "var/cache/CompileOnSaveHelper");
        }
        return new File(tmp, "CompileOnSaveHelper");
    }

    private static void copySigToClass(File sigDir, File clazzDir) throws IOException {
        if (!clazzDir.isDirectory() && !clazzDir.mkdirs()) {
            throw new IOException("could not create: " + clazzDir);
        }
        // First delete copies corresponding to missing originals:
        for (File child : clazzDir.listFiles()) {
            String n = child.getName();
            // XXX could also clean up empty dirs, but not really necessary
            if (child.isFile() && n.endsWith(".class")) {
                File orig = new File(sigDir, n.substring(0, n.length() - 6) + ".sig");
                if (!orig.isFile()) {
                    LOG.log(Level.FINER, "removing {0} since {1} does not exist", new Object[] {child, orig});
                    if (!child.delete()) {
                        throw new IOException("could not delete: " + child);
                    }
                }
            }
        }
        // Then copy original files to new:
        for (File child : sigDir.listFiles()) {
            String n = child.getName();
            if (child.isDirectory()) {
                copySigToClass(child, new File(clazzDir, n));
            } else if (n.endsWith(".sig")) {
                File copy = new File(clazzDir, n.substring(0, n.length() - 4) + ".class");
                if (copy.lastModified() <= child.lastModified()) { // including case where !copy.exists()
                    LOG.log(Level.FINER, "copying {0} to {1}", new Object[] {child, copy});
                    try {
                        InputStream is = new FileInputStream(child);
                        try {
                            OutputStream os = new FileOutputStream(copy);
                            try {
                                FileUtil.copy(is, os);
                            } finally {
                                os.close();
                            }
                        } finally {
                            is.close();
                        }
                    } catch (IOException x) {
                        throw (IOException) new IOException("could not copy " + child + " to " + copy + ": " + x).initCause(x);
                    }
                }
            }
        }
    }

}
