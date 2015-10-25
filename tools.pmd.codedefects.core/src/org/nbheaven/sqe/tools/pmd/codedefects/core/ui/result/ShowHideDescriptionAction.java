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

class ShowHideDescriptionAction extends AbstractAction {

    private static final Icon refreshIcon = new ImageIcon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/pmd/codedefects/core/resources/info.png"));
    private final ResultPanel resultPanel;

    ShowHideDescriptionAction(ResultPanel resultPanel) {
        super();
        this.resultPanel = resultPanel;
        putValue(SMALL_ICON, refreshIcon);
        putValue(NAME, "Show Description");
        putValue(SHORT_DESCRIPTION, "Show Description");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        resultPanel.updateDescription();
    }
}
