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
package org.nbheaven.sqe.core.ui.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Florian Vogler
 */
public class ActionUtils {

    public static ActionUtils getProjectUtils() {
        return new ActionUtils("SQE/Projects/Actions");
    }

    public static ActionUtils getPackageUtils() {
        return new ActionUtils("SQE/Packages/Actions");
    }
    
    private final FileObject fo;

    private ActionUtils(String folder) {
        this.fo = FileUtil.getConfigFile(folder);
    }

    public final Collection<JMenuItem> getMenuItems() {
        return fo != null ? createMenuList(fo) : Collections.<JMenuItem>emptySet();
    }

    private static Collection<JMenuItem> createMenuList(FileObject fo) {
        Collection<JMenuItem> myMenus = new ArrayList<JMenuItem>();

        for (FileObject actionsFileObject : fo.getChildren()) {
            try {
                if (actionsFileObject.isFolder()) {
                    JMenu myMenu = new JMenu(actionsFileObject.getName());
                    myMenu.setIcon(null);
                    Mnemonics.setLocalizedText(myMenu, myMenu.getText());
                    addActionsToSubMenu(actionsFileObject, myMenu);
                    myMenus.add(myMenu);
                } else {
                    DataObject dob = DataObject.find(actionsFileObject);
                    InstanceCookie cookie = dob.getCookie(InstanceCookie.class);

                    if (null != cookie) {
                        JMenuItem item = new JMenuItem();
                        Action action = (Action) cookie.instanceCreate();
                        Actions.connect(item, action, true);
                        myMenus.add(item);
                    }
                }
            } catch (DataObjectNotFoundException donfe) {
            } catch (IOException ioex) {
            } catch (ClassNotFoundException cnfe) {
            }
        }

        return myMenus;
    }

    private static void addActionsToSubMenu(FileObject fo, JMenu menu) {
        List<FileObject> children = new ArrayList<FileObject>(Arrays.asList(fo.getChildren()));
        children = FileUtil.getOrder(children, true);
        for (FileObject actionsFileObject : children) {
            try {
                if (actionsFileObject.isFolder()) {
                    // WARN here ??
                } else {
                    DataObject dob = DataObject.find(actionsFileObject);
                    InstanceCookie cookie = dob.getCookie(InstanceCookie.class);

                    if (null != cookie) {
                        Object instance = cookie.instanceCreate();
                        if (instance instanceof Action) {
                            JMenuItem item = new JMenuItem();
                            Actions.connect(item, (Action) instance, true);
                            menu.add(item);
                        } else if (instance instanceof JComponent) {
                            menu.add((JComponent) instance);
                        }
                    }
                }
            } catch (DataObjectNotFoundException donfe) {
            } catch (IOException ioex) {
            } catch (ClassNotFoundException cnfe) {
            }
        }
    }
}
