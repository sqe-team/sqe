/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbheaven.sqe.codedefects.ui.actions;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.actions.Presenter;

/**
 *
 * @author fvo
 */
public final class DropDownButtonToolbarPresenter {

    private DropDownButtonToolbarPresenter() {
    }

    public static Component createToolbarPresenter(Action buttonAction, List<Action> popupActions) {
        JPopupMenu dropdownPopup = new DropDownPopupMenu();
        for (Action action : popupActions) {
            dropdownPopup.add(createDropdownItem(action));
        }
        JButton button = DropDownButtonFactory.createDropDownButton(new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), dropdownPopup);
        Actions.connect(button, buttonAction);
        return button;
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
