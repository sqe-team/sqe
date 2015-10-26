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
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Florian Vogler
 */
public final class QualityProvidersToolbarDropdownAction implements Action, Presenter.Toolbar, ContextAwareAction {

    private List<? extends Action> actions;
    private final Action defaultAction;
    private JButton toolbarPresenter;
    private JPopupMenu dropdownPopup;
    private Lookup context;

    public QualityProvidersToolbarDropdownAction() {
        this(Utilities.actionsGlobalContext());
    }

    private QualityProvidersToolbarDropdownAction(Lookup context) {
        this.context = context;
        actions = findActions(context);
        defaultAction = actions.iterator().next();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new QualityProvidersToolbarDropdownAction(actionContext);
    }

    private static List<? extends Action> findActions(Lookup context) {
        return Utilities.actionsForPath("Menu/Quality/CodeDefects");
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
            dropdownPopup = Utilities.actionsToPopup(actions.toArray(new Action[0]), context);
            JButton button = DropDownButtonFactory.createDropDownButton(new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), dropdownPopup);
            Actions.connect(button, this);
            toolbarPresenter = button;
        }

        return toolbarPresenter;
    }

}
