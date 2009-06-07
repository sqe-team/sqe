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
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult.Mode;
import org.openide.util.NbBundle;

/**
 *
 * @author Sven Reimers
 * @author Florian Vogler
 */
class SortAction extends AbstractAction {

    private final BugTree bugTreePanel;
    private final Mode mode;

    SortAction(BugTree bugTreePanel, Mode mode) {
        super(NbBundle.getMessage(SortAction.class, mode.getHint()), mode.getIcon());
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(SortAction.class, mode.getHint()));
        this.bugTreePanel = bugTreePanel;
        this.mode = mode;
    }

    public void actionPerformed(final ActionEvent e) {
        bugTreePanel.setActiveMode(mode);
    }
}