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
package org.nbheaven.sqe.tools.pmd.codedefects.core.ui.result;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Sven Reimers
 */
class ExpandAllAction extends AbstractAction {

    private final static Icon expandIcon = new ImageIcon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/pmd/codedefects/core/resources/expandTree.png"));
    private final BugTree bugTreePanel;

    ExpandAllAction(BugTree bugTreePanel) {
        super("Expand", expandIcon);
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(RefreshAction.class, "HINT_EXPAND_ALL"));
        this.bugTreePanel = bugTreePanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        bugTreePanel.expandAll();
    }
}
