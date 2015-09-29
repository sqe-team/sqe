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
package org.nbheaven.sqe.core.ui.components.filter;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;


final class ArrowSignIcon implements Icon {
    private final ImageIcon popupArrowIcon = new ImageIcon(getClass()
                                                               .getResource("/org/nbheaven/sqe/core/ui/components/resources/popupArrow.png")); // NOI18N
    private Icon icon;

    public ArrowSignIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
        popupArrowIcon.paintIcon(c, g, (x + icon.getIconWidth()) - popupArrowIcon.getIconWidth(),
            (y + icon.getIconHeight()) - popupArrowIcon.getIconHeight());
    }
}
