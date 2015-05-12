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
package org.nbheaven.sqe.core.controlcenter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.core.api.SQEManager;
import org.nbheaven.sqe.core.ui.components.collapser.JCollapserPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
final class SQEControlCenterTopComponent extends TopComponent implements PropertyChangeListener {

    private static SQEControlCenterTopComponent instance;
    /** path to the icon used by the component and its open action */
    private static final String ICON_PATH = "org/nbheaven/sqe/core/controlcenter/ui/resources/sqe_16.png";
    private static final String PREFERRED_ID = "SQEControlCenterTopComponent";
    private static final String TC_GROUP = "SQEControlCenter";

    private SQEControlCenterTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(SQEControlCenterTopComponent.class, "CTL_SQEControlCenterTopComponent"));
        setToolTipText(NbBundle.getMessage(SQEControlCenterTopComponent.class, "HINT_SQEControlCenterTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        initComponentsManual();
    }

    private void initComponentsManual() {
        this.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new VerticalLayout());
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        FileObject fo = FileUtil.getConfigFile("SQE/ControlCenter/Panels");
        for (FileObject panelObject : FileUtil.getOrder(Arrays.asList(fo.getChildren()), true)) {
            try {
                DataObject dao = DataObject.find(panelObject);
                Boolean collapsed = (Boolean) panelObject.getAttribute("collapsed");
                InstanceCookie cookie = dao.getLookup().lookup(InstanceCookie.class);

                if (null != cookie) {
                    JComponent component = (JComponent) cookie.instanceCreate();
                    JCollapserPanel snippet = new JCollapserPanel(dao.getNodeDelegate().getDisplayName(), component);
                    if (null != collapsed && collapsed) {
                        snippet.setCollapsed(true);
                    }
                    panel.add(snippet, gbc);
                }

            } catch (DataObjectNotFoundException donfe) {
                Exceptions.printStackTrace(donfe);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } catch (ClassNotFoundException cnfe) {
                Exceptions.printStackTrace(cnfe);
            }
        }

        gbc.weighty = 1;
        JPanel padding = new JPanel();
        padding.setBackground(Color.WHITE);
        padding.setOpaque(true);
        panel.add(padding, gbc);

        JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //spControls.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, CP_BACKGROUND_COLOR));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(50);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(50);
        add(scrollPane, BorderLayout.CENTER);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized SQEControlCenterTopComponent getDefault() {
        if (instance == null) {
            instance = new SQEControlCenterTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the SQEControlCenterTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized SQEControlCenterTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(SQEControlCenterTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof SQEControlCenterTopComponent) {
            return (SQEControlCenterTopComponent) win;
        }
        Logger.getLogger(SQEControlCenterTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup(TC_GROUP);
        if (group != null) {
            group.open();
        }
    }

    @Override
    public void componentDeactivated() {
        // close window group containing propsheet, but only if we're
        // selecting a different kind of TC in the same mode
        boolean closeGroup = true;
        Mode curMode = WindowManager.getDefault().findMode(this);
        TopComponent selected = curMode.getSelectedTopComponent();
        if (selected != null && selected instanceof SQEControlCenterTopComponent) {
            closeGroup = false;
        }
        if (closeGroup) {
            TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup(TC_GROUP);
            if (group != null) {
                group.close();
            }
        }

        super.componentDeactivated();
    }

    @Override
    public void componentOpened() {
        SQEManager.getDefault().setFollowActionGlobalContext(false);
        SQEManager.getDefault().addPropertyChangeListener(SQEManager.PROP_ACTIVE_PROJECT, this);
        setActiveProject(SQEManager.getDefault().getActiveProject());
    }

    @Override
    public void componentClosed() {
        SQEManager.getDefault().removePropertyChangeListener(SQEManager.PROP_ACTIVE_PROJECT, this);
        SQEManager.getDefault().setFollowActionGlobalContext(true);
        super.componentClosed();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setActiveProject((Project) evt.getNewValue());
    }

    private void setActiveProject(final Project project) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    setActiveProject(project);
                }
            });
            return;
        }

        assert SwingUtilities.isEventDispatchThread();

        if (null != project) {
            setDisplayName("Quality [" + ProjectUtils.getInformation(project).getDisplayName() + "]"); // XXX I18N
            Node[] activatedNodes = new Node[]{new AbstractNode(Children.LEAF, Lookups.fixed(project))};
            setActivatedNodes(activatedNodes);

        } else {
            setDisplayName("Quality [no project]"); // XXX I18N
            setActivatedNodes(new Node[]{});
        }
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return SQEControlCenterTopComponent.getDefault();
        }
    }

    public static final class VerticalLayout implements LayoutManager {

        public void addLayoutComponent(final String name, final Component comp) {
        }

        public void layoutContainer(final Container parent) {
            final Insets insets = parent.getInsets();
            final int posX = insets.left;
            int posY = insets.top;
            final int width = parent.getWidth() - insets.left - insets.right;

            final Component[] comps = parent.getComponents();

            for (int i = 0; i < comps.length; i++) {
                final Component comp = comps[i];

                if (comp.isVisible()) {
                    int height = comp.getPreferredSize().height;

                    if (i == (comps.length - 1)) // last component
                    {
                        if ((posY + height) < (parent.getHeight() - insets.bottom)) {
                            height = parent.getHeight() - insets.bottom - posY;
                        }
                    }

                    comp.setBounds(posX, posY, width, height);
                    posY += height;
                }
            }
        }

        public Dimension minimumLayoutSize(final Container parent) {
            final Dimension d = new Dimension(parent.getInsets().left + parent.getInsets().right,
                    parent.getInsets().top + parent.getInsets().bottom);
            int maxWidth = 0;
            int height = 0;
            final Component[] comps = parent.getComponents();

            for (int i = 0; i < comps.length; i++) {
                final Component comp = comps[i];

                if (comp.isVisible()) {
                    final Dimension size = comp.getMinimumSize();
                    maxWidth = Math.max(maxWidth, size.width);
                    height += size.height;
                }
            }

            d.width += maxWidth;
            d.height += height;

            return d;
        }

        public Dimension preferredLayoutSize(final Container parent) {
            final Dimension d = new Dimension(parent.getInsets().left + parent.getInsets().right,
                    parent.getInsets().top + parent.getInsets().bottom);
            int maxWidth = 0;
            int height = 0;
            final Component[] comps = parent.getComponents();

            for (int i = 0; i < comps.length; i++) {
                final Component comp = comps[i];

                if (comp.isVisible()) {
                    final Dimension size = comp.getPreferredSize();
                    maxWidth = Math.max(maxWidth, size.width);
                    height += size.height;
                }
            }

            d.width += maxWidth;
            d.height += height;

            return d;
        }

        public void removeLayoutComponent(final Component comp) {
        }
    }
}
