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
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;


final class StringComboBox extends JComboBox {
    public JTextComponent comboEditor;

    public boolean comboPopupActionFlag = false;
    public boolean comboPopupKeyFlag = false;

    public StringComboBox() {
        super();
        comboEditor = (JTextComponent) getEditor().getEditorComponent();
        setRenderer(new Renderer());
        addPopupMenuListener(new MenuListener(this));
    }

    public void setText(String text) {
        comboEditor.setText(text);
    }

    public String getText() {
        return comboEditor.getText();
    }

    public void clearSelection() {
        comboEditor.setSelectionStart(comboEditor.getCaretPosition());
        comboEditor.setSelectionEnd(comboEditor.getCaretPosition());
    }
    
    private class Renderer extends DefaultListCellRenderer {
        private JSeparator separator;

        public Renderer() {
            super();
            separator = new JSeparator();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            JComponent renderer = (JComponent) super.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus);
            renderer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(
                        0, 3, 0, 0), renderer.getBorder()));
            return renderer;
        }
    }

    private class MenuListener implements PopupMenuListener {
        
        private StringComboBox box;
        
        MenuListener(StringComboBox box) {
            this.box = box;
        }
        
        public void popupMenuCanceled(PopupMenuEvent e) {
            box.comboPopupActionFlag = false;
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            box.comboPopupActionFlag = false;
            box.clearSelection();
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            box.comboPopupActionFlag = true;
        }
    }

    
}
