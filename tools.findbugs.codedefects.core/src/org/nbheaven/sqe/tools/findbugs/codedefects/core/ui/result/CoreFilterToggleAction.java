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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.ui.result;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author sven
 */
class CoreFilterToggleAction extends AbstractAction implements PropertyChangeListener {

    private final static Icon coreFilterDisabled = new ImageIcon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/bullseye_off.png"));
    private final static Icon coreFilterEnabled = new ImageIcon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/bullseye.png"));
    private final BugTree bugTreePanel;

    CoreFilterToggleAction(BugTree bugTreePanel) {
        this.bugTreePanel = bugTreePanel;
        updateState();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        bugTreePanel.setCoreFilterEnabled(!bugTreePanel.isCoreFilterEnabled());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateState();
    }

    private void updateState() {
        if (bugTreePanel.isCoreFilterEnabled()) {
            putValue(NAME, "Disable Core Filter");
            putValue(SMALL_ICON, coreFilterDisabled);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(RefreshAction.class, "HINT_CORE_FILTER_OFF"));

        } else {
            putValue(NAME, "Enable Core Filter");
            putValue(SMALL_ICON, coreFilterEnabled);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(RefreshAction.class, "HINT_CORE_FILTER_ON"));

        }
    }
}
