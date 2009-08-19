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
package org.nbheaven.sqe.core.ui.options;

import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;

import org.openide.util.NbBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;


public final class UiOptionsCategory extends OptionsCategory {
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage(
                "org/nbheaven/sqe/core/ui/resources/sqe_32.png"));
    }

    public String getCategoryName() {
        return NbBundle.getMessage(UiOptionsCategory.class,
            "OptionsCategory_Name");
    }

    public String getTitle() {
        return NbBundle.getMessage(UiOptionsCategory.class,
            "OptionsCategory_Title");
    }

    public OptionsPanelController create() {
        return OptionsPanelController.createAdvanced("SQE");
    }
}
