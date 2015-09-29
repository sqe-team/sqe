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

import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.PluginException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

class Installer {

    private Installer() {
    }

    private static FileChangeListener updater;

    /**
     * Prepares FindBugs plugins.
     */
    public static synchronized void installPluginUpdater() {
        final FileObject fo = FileUtil.getConfigFile("FindBugs/Plugins");
        if (null != fo) {
            updater = new PluginListChangedListener(fo);
            fo.addFileChangeListener(updater);
            updatePluginList(fo);
        }
    }

    private static void updatePluginList(FileObject fo) {
        for (FileObject pluginsFileObject : fo.getChildren()) {
            Object jar = pluginsFileObject.getAttribute("jar");
            try {
                Plugin.addCustomPlugin((URL) jar);
            } catch (PluginException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static class PluginListChangedListener implements FileChangeListener {

        private FileObject fo;

        PluginListChangedListener(FileObject fo) {
            this.fo = fo;
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
            updatePluginList(fo);
        }

        @Override
        public void fileChanged(FileEvent fileEvent) {
            updatePluginList(fo);
        }

        @Override
        public void fileDataCreated(FileEvent fileEvent) {
            updatePluginList(fo);
        }

        @Override
        public void fileDeleted(FileEvent fileEvent) {
            updatePluginList(fo);
        }

        @Override
        public void fileFolderCreated(FileEvent fileEvent) {
            updatePluginList(fo);
        }

        @Override
        public void fileRenamed(FileRenameEvent fileRenameEvent) {
            updatePluginList(fo);
        }
    }
}
