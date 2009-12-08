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
package org.nbheaven.sqe.core.ui.components.toolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicToolBarUI;

/**
 *
 * @author Sven Reimers
 */
public class FlatToolBar extends JToolBar {

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class FlatMarginBorder extends AbstractBorder {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public Insets getBorderInsets(Component c) {
            return getBorderInsets(c, new Insets(0, 0, 0, 0));
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets margin = null;

            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                margin = b.getMargin();
            }

            insets.top = (margin != null) ? margin.top : 0;
            insets.left = (margin != null) ? margin.left : 0;
            insets.bottom = (margin != null) ? margin.bottom : 0;
            insets.right = (margin != null) ? margin.right : 0;

            return insets;
        }
    }

    /**
     * Special thin border for rollover toolbar buttons.
     */
    public static class FlatRolloverButtonBorder extends AbstractBorder {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Color normal;
        private Color pressed;
        private Color roll;
        private boolean borderPainted = false;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public FlatRolloverButtonBorder(Color pressed, Color roll) {
            super();
            this.pressed = pressed;
            this.roll = roll;
            this.borderPainted = false;
        }

        public FlatRolloverButtonBorder(Color pressed, Color roll, Color normal) {
            super();
            this.pressed = pressed;
            this.roll = roll;
            this.normal = normal;
            this.borderPainted = true;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public Insets getBorderInsets(Component c) {
            return getBorderInsets(c, new Insets(0, 0, 0, 0));
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            // leave room for default visual
            insets.top = 2;
            insets.left = insets.bottom = insets.right = 3;

            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();

            if ((model.isRollover() && !(model.isPressed() && !model.isArmed())) || model.isSelected()) {
                Color oldColor = g.getColor();
                g.translate(x, y);

                if ((model.isPressed() && model.isArmed()) || model.isSelected()) {
                    // Draw the pressd button
                    g.setColor(pressed);
                    g.drawRect(0, 0, w - 1, h - 1);
                } else {
                    // Draw a rollover button
                    g.setColor(roll);
                    g.drawRect(0, 0, w - 1, h - 1);
                }

                g.translate(-x, -y);
                g.setColor(oldColor);
            } else if (borderPainted) {
                Color oldColor = g.getColor();
                g.translate(x, y);
                g.setColor(normal);
                g.drawRect(0, 0, w - 1, h - 1);
                g.translate(-x, -y);
                g.setColor(oldColor);
            }
        }
    }

    private static class MyToolBarUI extends BasicToolBarUI {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private AbstractBorder myRolloverBorder;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public MyToolBarUI() {
            myRolloverBorder = new CompoundBorder(new FlatRolloverButtonBorder(Color.GRAY, Color.LIGHT_GRAY),
                                                  new FlatMarginBorder());
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        protected void setBorderToRollover(Component c) {
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;

                if (b.getBorder() instanceof UIResource) {
                    b.setBorder(myRolloverBorder);
                }

                b.setRolloverEnabled(true);
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final int BUTTON_STYLE_HORIZONTAL = 1;
    public static final int BUTTON_STYLE_VERICAL = 2;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    int buttonStyle = BUTTON_STYLE_HORIZONTAL;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a horizontal toolbar with horizontal button style (i.e. icon left to text)
     */
    public FlatToolBar() {
        this(HORIZONTAL, BUTTON_STYLE_HORIZONTAL);
    }

    /**
     * Creates a toolbar with specified orientation and horizontal button style (i.e. icon left to text)
     *
     * @see JToolBar.HORIZONTAL
     * @see JToolBar.VERTICAL
     * @param orientation
     */
    public FlatToolBar(int orientation) {
        this(orientation, BUTTON_STYLE_HORIZONTAL);
    }

    /**
     * Creates a toolbar with specified orientation and button style
     *
     * @see JToolBar.HORIZONTAL
     * @see JToolBar.VERTICAL
     *
     * @param orientation
     * @param buttonStyle
     */
    public FlatToolBar(int orientation, int buttonStyle) {
        super(orientation);
        this.buttonStyle = buttonStyle;

        if (!UIManager.getLookAndFeel().getID().equals("GTK")) {
            setUI(new MyToolBarUI());
        }

        setFloatable(false);
        setOpaque(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected @Override void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof AbstractButton) {
            AbstractButton ab = (AbstractButton) comp;
            ab.setContentAreaFilled(false);
            ab.setMargin(new Insets(3, 3, 3, 3));

            if (buttonStyle == BUTTON_STYLE_VERICAL) {
                ab.setVerticalTextPosition(SwingConstants.BOTTOM);
                ab.setHorizontalTextPosition(SwingConstants.CENTER);
            }
        }

        super.addImpl(comp, constraints, index);
    }

}
