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
package org.nbheaven.sqe.core.controlcenter.ui.panels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;
import org.nbheaven.sqe.core.api.SQEManager;
import org.nbheaven.sqe.core.ui.components.toolbar.FlatToolBar;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Sven Reimers
 */
public class Controls extends FlatToolBar {

    public Controls() {
        init();
    }

    private void init() {
        for (Action action : getActions()) {
            if (action instanceof Presenter.Toolbar) {
                add(((Presenter.Toolbar) action).getToolbarPresenter());
            } else {
                add(action);
            }
        }
        this.setOpaque(false);
    }

    private Collection<Action> getActions() {
        Lookup context = SQEManager.getDefault().getLookup();

        Collection<Action> actions = new ArrayList<Action>();
        FileObject fo = FileUtil.getConfigFile("SQE/ControlCenter/Controls");
        for (FileObject actionsFileObject : FileUtil.getOrder(Arrays.asList(fo.getChildren()), true)) {
            try {
                DataObject dob = DataObject.find(actionsFileObject);
                InstanceCookie cookie = dob.getLookup().lookup(InstanceCookie.class);
                if (null != cookie) {
                    Action action = (Action) cookie.instanceCreate();
                    if (action instanceof ContextAwareAction) {
                        action = ((ContextAwareAction) action).createContextAwareInstance(context);
                    }
                    actions.add(action);
                }
            } catch (DataObjectNotFoundException donfe) {
            } catch (IOException ioex) {
            } catch (ClassNotFoundException cnfe) {
            }
        }
        return actions;
    }
}
