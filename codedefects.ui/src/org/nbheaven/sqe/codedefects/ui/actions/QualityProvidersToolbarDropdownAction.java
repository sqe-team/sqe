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
package org.nbheaven.sqe.codedefects.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author fvo
 */
public class QualityProvidersToolbarDropdownAction implements Action, Presenter.Toolbar, ContextAwareAction {

    private List<Action> providerActions;
    private final Action defaultAction;
    private JButton toolbarPresenter;
    private JPopupMenu dropdownPopup;

    public QualityProvidersToolbarDropdownAction() {
        this(Utilities.actionsGlobalContext());
    }

    private QualityProvidersToolbarDropdownAction(Lookup context) {
        providerActions = findProviderActions(context);
        defaultAction = providerActions.iterator().next();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new QualityProvidersToolbarDropdownAction(actionContext);
    }

    private static List<Action> findProviderActions(Lookup context) {
        // XXX could probably use Utilities.actionsForPath + Utilities.actionsToPopup
        FileObject actionsFO = FileUtil.getConfigFile("Menu/Quality/CodeDefects");
        ArrayList<Action> actions = new ArrayList<Action>();
        for (FileObject actionsFileObject : FileUtil.getOrder(Arrays.asList(actionsFO.getChildren()), true)) {
            try {
                if (actionsFileObject.isData()) {
                    DataObject dob = DataObject.find(actionsFileObject);
                    InstanceCookie cookie = dob.getLookup().lookup(InstanceCookie.class);
                    if (null != cookie) {
                        Action action = (Action) cookie.instanceCreate();
                        if (null != context && action instanceof ContextAwareAction) {
                            actions.add(((ContextAwareAction) action).createContextAwareInstance(context));
                        } else {
                            actions.add(action);
                        }
                    }
                }
            } catch (DataObjectNotFoundException donfe) {
            } catch (IOException ioex) {
            } catch (ClassNotFoundException cnfe) {
            }
        }
        actions.trimToSize();
        return actions.isEmpty() ? Collections.<Action>emptyList() : actions;
    }

    @Override
    public void setEnabled(boolean b) {
        defaultAction.setEnabled(b);
    }

    @Override
    public boolean isEnabled() {
        return defaultAction.isEnabled();
    }

    @Override
    public Object getValue(String key) {
        return defaultAction.getValue(key);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        defaultAction.actionPerformed(e);
    }

    @Override
    public void putValue(String key, Object value) {
        defaultAction.putValue(key, value);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        defaultAction.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        defaultAction.removePropertyChangeListener(listener);
    }

    @Override
    public Component getToolbarPresenter() {
        if (toolbarPresenter == null) {

            dropdownPopup = new DropDownPopupMenu();
            for (Action action : providerActions) {
                dropdownPopup.add(createDropdownItem(action));
            }

            JButton button = DropDownButtonFactory.createDropDownButton(new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), dropdownPopup);
            Actions.connect(button, this);
//            defaultAction.addPropertyChangeListener(new PropertyChangeListener() {
//
//                public void propertyChange(PropertyChangeEvent evt) {
//                    if (defaultAction.isEnabled()) {
//                        toolbarPresenter.putClientProperty(DropDownButtonFactory.PROP_DROP_DOWN_MENU, dropdownPopup);
//                    } else {
//                        toolbarPresenter.putClientProperty(DropDownButtonFactory.PROP_DROP_DOWN_MENU, null);
//                    }
//                }
//            });

            toolbarPresenter = button;
        }

        return toolbarPresenter;
    }

    private static JMenuItem createDropdownItem(final Action action) {
        JMenuItem item;
        if (action instanceof Presenter.Popup) {
            item = ((Presenter.Popup) action).getPopupPresenter();
        } else {
            item = new JMenuItem(action);
        }
        Actions.connect(item, action, true);
        return item;
    }

    private static class DropDownPopupMenu extends JPopupMenu {

        private DropDownPopupMenu() {
        }

        @Override
        public void setVisible(boolean visible) {
            for (Component c : getComponents()) {
                if (c instanceof Actions.MenuItem) {
                    Actions.MenuItem item = (Actions.MenuItem) c;
                    item.synchMenuPresenters(null); // Hack to force item state update
                } else if (c instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) c;
                    Action action = item.getAction();
                    if (null != action) {
                        item.setEnabled(action.isEnabled());
                    }
                }
            }
            super.setVisible(visible);
        }
    }
}