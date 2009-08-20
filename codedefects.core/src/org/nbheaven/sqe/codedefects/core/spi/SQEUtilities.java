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
package org.nbheaven.sqe.codedefects.core.spi;

import java.util.Arrays;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;

import org.openide.cookies.InstanceCookie;

import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;


/**
 *
 * @author sven
 */
public class SQEUtilities {
    private static Collection<QualityProvider> providers;

    /** Creates a new instance of SQEUtilities */
    private SQEUtilities() {
    }

    public static synchronized Collection<QualityProvider> getProviders() {
        if (null == providers) {
            final FileObject fo = FileUtil.getConfigFile("SQE/Providers/CodeDefects");

            if (null != fo) {
                fo.addFileChangeListener(new FileChangeListener() {
                        public void fileAttributeChanged(
                            FileAttributeEvent fileAttributeEvent) {
                            providers = createProviderList(fo);
                        }

                        public void fileChanged(FileEvent fileEvent) {
                            providers = createProviderList(fo);
                        }

                        public void fileDataCreated(FileEvent fileEvent) {
                            providers = createProviderList(fo);
                        }

                        public void fileDeleted(FileEvent fileEvent) {
                            providers = createProviderList(fo);
                        }

                        public void fileFolderCreated(FileEvent fileEvent) {
                            providers = createProviderList(fo);
                        }

                        public void fileRenamed(FileRenameEvent fileRenameEvent) {
                            providers = createProviderList(fo);
                        }
                    });
                providers = createProviderList(fo);
            }
        }

        return providers;
    }

    private static Collection<QualityProvider> createProviderList(FileObject fo) {
        Collection<QualityProvider> myProviders = new ArrayList<QualityProvider>();

        for (FileObject actionsFileObject : FileUtil.getOrder(Arrays.asList(fo.getChildren()), true)) {
            try {
                DataObject dob = DataObject.find(actionsFileObject);
                InstanceCookie cookie = dob.getCookie(InstanceCookie.class);

                if (null != cookie) {
                    myProviders.add((QualityProvider) cookie.instanceCreate());
                }
            } catch (DataObjectNotFoundException donfe) {
            } catch (IOException ioex) {
            } catch (ClassNotFoundException cnfe) {
            }
        }

        return myProviders;
    }
}
