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
package org.nbheaven.sqe.core.ui.components.collapser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class JCollapserPanel extends JPanel implements MouseListener, KeyListener, FocusListener {

    public static class Padding extends JPanel {

        public Padding() {
            setBackground(Color.WHITE);
            setOpaque(true);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(lineColor);
            g.drawLine(0, 0, getWidth(), 0);
        }
    }

    private static class Title extends JComponent implements Accessible {
        String name;
        private boolean collapsed;
        private boolean rollOver;

        private Title(String name) {
            this.name = name;
            setUI(new TitleUI());
        }

        public void setRollOver(boolean rollOver) {
            if (rollOver == this.rollOver) {
                return;
            }

            this.rollOver = rollOver;
            repaint();
        }

        public void collapse() {
            collapsed = true;
            repaint();
        }

        public void expand() {
            collapsed = false;
            repaint();
        }
    }

    private static class TitleUI extends ComponentUI {
        ImageIcon collapsedIcon = new ImageIcon(JCollapserPanel.class.getResource("/org/nbheaven/sqe/core/ui/components/resources/collapsedSnippet.png")); //NOI18N
        ImageIcon expandedIcon = new ImageIcon(JCollapserPanel.class.getResource("/org/nbheaven/sqe/core/ui/components/resources/expandedSnippet.png")); //NOI18N

        public @Override Dimension getPreferredSize(JComponent c) {
            try {
                Graphics graphics = c.getGraphics();
                Font font = c.getFont();
                FontMetrics fm = graphics.getFontMetrics(font);
                Rectangle2D bounds = fm.getStringBounds(((Title) c).name, graphics);
                Rectangle boundsRect = bounds.getBounds();
                return new Dimension(20 /* 20 is hardcoded x-offset for title string in paint(Graphics g, JComponent c)*/
                                     + boundsRect.width, fm.getHeight() + 4);
            } catch (NullPointerException x) {
                Logger.getLogger(JCollapserPanel.class.getName()).log(Level.INFO, "#167812", x);
                return super.getPreferredSize(c);
            }
        }

        public void installUI(JComponent c) {
            Font f = UIManager.getFont("Label.font"); //NOI18N
            c.setFont(f.deriveFont(Font.BOLD));
        }

        public void paint(Graphics g, JComponent c) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Title title = (Title) c;
            Font font = c.getFont();

            if (title.collapsed) { // use plain font if collapsed
                g.setFont(font.deriveFont(Font.PLAIN));
            } else {
                g.setFont(font);
            }

            g.setColor(lineColor);

            FontMetrics fm = g.getFontMetrics(font);

            g.drawLine(0, 0, c.getWidth(), 0);

            if (title.collapsed) { // do not draw bottom line if collapsed

                if (title.rollOver || title.isFocusOwner()) {
                    g.setColor(focusedBackgroundColor);
                } else {
                    g.setColor(backgroundColor);
                }
            }

            g.drawLine(0, 1 + fm.getHeight() + 2, c.getWidth(), 1 + fm.getHeight() + 2);

            if (title.rollOver || title.isFocusOwner()) {
                g.setColor(focusedBackgroundColor);
            } else {
                g.setColor(backgroundColor);
            }

            g.fillRect(0, 1, c.getWidth(), fm.getHeight() + 2);

            g.setColor(textColor);
            g.drawString(title.name, 20, fm.getHeight() - 1);

            int iconX = 5;
            int iconY = 5;
            ImageIcon icon = title.collapsed ? collapsedIcon : expandedIcon;

            icon.paintIcon(c, g, iconX, iconY);
        }
    }

    private static Color lineColor;
    private static Color backgroundColor;
    private static Color focusedBackgroundColor;
    private static Color textColor;
    
    static { initColors(); }

    private JComponent content;
    private String snippetName;
    private Title title;
    private boolean collapsed = false;

    public JCollapserPanel(String snippetName, JComponent content) {
        this.snippetName = snippetName;
        this.content = content;
        this.setBackground(Color.WHITE);
        this.setOpaque(true);
        setLayout(new BorderLayout());
        title = new Title(snippetName) {
                public AccessibleContext getAccessibleContext() {
                    return JCollapserPanel.this.getAccessibleContext();
                }
            };
        title.setFocusable(true);
        title.addKeyListener(this);
        title.addMouseListener(this);
        title.addFocusListener(this);
        // transfer the tooltip from the content to the snippet panel
        title.setToolTipText(content.getToolTipText());
        content.setToolTipText(null);
        //**
        add(title, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        getAccessibleContext().setAccessibleName(snippetName);
    }

    private static Color getSafeColor(int red, int green, int blue) {
        red = Math.max(red, 0);
        red = Math.min(red, 255);
        green = Math.max(green, 0);
        green = Math.min(green, 255);
        blue = Math.max(blue, 0);
        blue = Math.min(blue, 255);

        return new Color(red, green, blue);
    }

    private static void initColors() {
        Color systemBackgroundColor = Color.WHITE;
        
        int backgroundRed = systemBackgroundColor.getRed(); 
        int backgroundGreen = systemBackgroundColor.getGreen();
        int backgroundBlue = systemBackgroundColor.getBlue();
        boolean inverseColors = backgroundRed < 41 || backgroundGreen < 32 || backgroundBlue < 25;

        if (inverseColors) {
            lineColor = getSafeColor(backgroundRed + 41, backgroundGreen + 32, backgroundBlue + 8);
            backgroundColor = getSafeColor(backgroundRed + 7, backgroundGreen + 7, backgroundBlue + 7);
            focusedBackgroundColor = getSafeColor(backgroundRed + 25, backgroundGreen + 25, backgroundBlue + 25);
        } else {
            lineColor = getSafeColor(backgroundRed - 41 /*214*/, backgroundGreen - 32 /*223*/, backgroundBlue - 8 /*247*/);
            backgroundColor = getSafeColor(backgroundRed - 7 /*248*/, backgroundGreen - 7 /*248*/, backgroundBlue - 7 /*248*/);
            focusedBackgroundColor = getSafeColor(backgroundRed - 25 /*230*/, backgroundGreen - 25 /*230*/, backgroundBlue - 25 /*230*/);
        }
        
        textColor = UIManager.getColor("Button.foreground"); // NOI18N
    }

    public void setCollapsed(boolean collapsed) {
        if (this.collapsed == collapsed) {
            return;
        }

        this.collapsed = collapsed;

        if (collapsed) {
            title.collapse();
        } else {
            title.expand();
        }

        content.setVisible(!collapsed);
        revalidate();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setContent(JComponent content) {
        this.content = content;
    }

    public JComponent getContent() {
        return content;
    }

    public void setSnippetName(String snippetName) {
        this.snippetName = snippetName;
    }

    public String getSnippetName() {
        return snippetName;
    }

    public void focusGained(FocusEvent e) {
        title.repaint();
    }

    public void focusLost(FocusEvent e) {
        title.repaint();
    }

    public void keyPressed(final KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            setCollapsed(!isCollapsed());
        }
    }

    public void keyReleased(final KeyEvent evt) {
    } // not used

    public void keyTyped(final KeyEvent evt) {
    } // not used

    public void mouseClicked(MouseEvent e) {
    } // not used

    public void mouseEntered(MouseEvent e) {
        title.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        title.setRollOver(true);
    }

    public void mouseExited(MouseEvent e) {
        title.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        title.setRollOver(false);
    }

    public void mousePressed(MouseEvent e) {
        setCollapsed(!collapsed);
        requestFocus();
    }

    public void mouseReleased(MouseEvent e) {
    } // not used

    public void requestFocus() {
        if (title != null) {
            title.requestFocus();
        }
    }
}
