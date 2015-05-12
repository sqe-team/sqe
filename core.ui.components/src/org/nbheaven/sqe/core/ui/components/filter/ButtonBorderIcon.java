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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

final class ButtonBorderIcon implements Icon {
    private Color BUTTON_BORDER_DARK = new Color(115, 115, 115);
    private Color BUTTON_BORDER_LIGHT = new Color(204, 204, 204);
    private Icon icon;

    public ButtonBorderIcon(Icon icon) {
        this.icon = icon;
    }

    public int getIconHeight() {
        return icon.getIconHeight();
    }

    public int getIconWidth() {
        return icon.getIconWidth();
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
        g.setColor(BUTTON_BORDER_DARK);
        g.drawLine((x + icon.getIconWidth()) - 1, y, (x + icon.getIconWidth()) - 1,
            (y + icon.getIconHeight()) - 1);
        g.drawLine(x, (y + icon.getIconHeight()) - 1, (x + icon.getIconWidth()) - 1,
            (y + icon.getIconHeight()) - 1);
        g.setColor(BUTTON_BORDER_LIGHT);
        g.drawLine(x, y, (x + icon.getIconWidth()) - 1, y);
        g.drawLine(x, y, x, (y + icon.getIconHeight()) - 1);
    }
}
