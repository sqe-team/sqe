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

package org.nbheaven.sqe.core.ui.components.toolbar;

import java.awt.Component;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * Utility for connecting layer entries to a toolbar.
 * Similar to {@link Utilities#actionsForPath} and {@link Utilities#actionsToPopup(Action[], Lookup)}.
 */
public class ToolBarFromLayer {

    private ToolBarFromLayer() {}

    /**
     * Makes a toolbar display actions or other components registered in a layer path.
     * Dynamic changes to the layer should result in changes to the toolbar.
     * Actions may wish to set the property {@code hideActionText} to true.
     * @param toolBar a toolbar which will be fully populated
     * @param path a layer path to load actions from
     * @param context context to pass to any context-sensitive actions, or null
     * @param largeIcons true to try to use 24x24 icons for actions defining {@code iconBase},
     *                   false to use the default (usually 16x16) icons
     */
    public static void connect(final JToolBar toolBar, String path, final Lookup context, final boolean largeIcons) {
        final Lookup.Result<Object> actions = Lookups.forPath(path).lookupResult(Object.class);
        LookupListener listener = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        toolBar.removeAll();
                        for (Object item : actions.allInstances()) {
                            if (context != null && item instanceof ContextAwareAction) {
                                item = ((ContextAwareAction) item).createContextAwareInstance(context);
                            }
                            Component c;
                            if (item instanceof Presenter.Toolbar) {
                                c = ((Presenter.Toolbar) item).getToolbarPresenter();
                            } else if (item instanceof Action) {
                                JButton button = new JButton();
                                Actions.connect(button, (Action) item);
                                if (largeIcons) {
                                    button.putClientProperty("PreferredIconSize", 24); // NOI18N
                                }
                                c = button;
                            } else if (item instanceof Component) {
                                c = (Component) item;
                            } else {
                                if (item != null) {
                                    Logger.getLogger(ToolBarFromLayer.class.getName()).warning("Unknown object: " + item);
                                }
                                continue;
                            }
                            toolBar.add(c);
                        }
                    }
                });
            }
        };
        actions.addLookupListener(listener);
        toolBar.putClientProperty("actionsLookupResult", actions); // prevent GC
        listener.resultChanged(null);
    }

}
