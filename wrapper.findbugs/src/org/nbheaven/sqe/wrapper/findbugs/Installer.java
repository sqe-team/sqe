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
package org.nbheaven.sqe.wrapper.findbugs;

import edu.umd.cs.findbugs.DetectorFactoryCollection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    private static FileChangeListener updater;

    public void restored() {
        installPluginUpdater();
    }

    public static synchronized void installPluginUpdater() {
        final FileObject fo = FileUtil.getConfigFile("FindBugs/Plugins");
        if (null != fo) {
            updater = new PluginListChangedListener(fo);
            fo.addFileChangeListener(updater);
            updatePluginList(fo);
        }
    }

    private static void updatePluginList(FileObject fo) {
        Collection<URL> urls = new ArrayList<URL>();
        for (FileObject pluginsFileObject : fo.getChildren()) {
            Object jar = pluginsFileObject.getAttribute("jar");
            urls.add((URL) jar);
        }
        DetectorFactoryCollection.rawInstance().setPluginList(urls.toArray(new URL[urls.size()]));
    }

    private static class PluginListChangedListener implements FileChangeListener {

        private FileObject fo;

        PluginListChangedListener(FileObject fo) {
            this.fo = fo;
        }

        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
            updatePluginList(fo);
        }

        public void fileChanged(FileEvent fileEvent) {
            updatePluginList(fo);
        }

        public void fileDataCreated(FileEvent fileEvent) {
            updatePluginList(fo);
        }

        public void fileDeleted(FileEvent fileEvent) {
            updatePluginList(fo);
        }

        public void fileFolderCreated(FileEvent fileEvent) {
            updatePluginList(fo);
        }

        public void fileRenamed(FileRenameEvent fileRenameEvent) {
            updatePluginList(fo);
        }
    }
}
