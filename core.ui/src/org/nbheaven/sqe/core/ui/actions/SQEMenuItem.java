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

import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author florian
 */
public abstract class SQEMenuItem extends NodeAction implements Presenter.Popup {

    private static final String name = NbBundle.getBundle(SQEMenuItem.class).getString("CTL_MenuItem_SQECommands_Label");

    protected SQEMenuItem() {
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final HelpCtx getHelpCtx() {
        return new HelpCtx(SQEMenuItem.class);
    }

    @Override
    protected final void performAction(Node[] nodes) {
        // this item does nothing, it displays a popup
    }

    @Override
    protected abstract boolean enable(Node[] nodes);

    @Override
    public final JMenuItem getPopupPresenter() {
        return new SQEMenu();
    }

    protected abstract ActionUtils getActionUtils();

    private final class SQEMenu extends JMenu {

        private boolean popupConstructed;

        public SQEMenu() {
            Mnemonics.setLocalizedText(this, SQEMenuItem.this.getName());
        }

        @Override
        public void setSelected(boolean selected) {
            if (selected && (!popupConstructed)) {
                // lazy submenu construction
                for (JMenuItem menu : getActionUtils().getMenuItems()) {
                    add(menu);
                }
                if (0 == getItemCount()) {
                    setEnabled(false);
                }
                popupConstructed = true;
            }

            super.setSelected(selected);
        }
    }
}
