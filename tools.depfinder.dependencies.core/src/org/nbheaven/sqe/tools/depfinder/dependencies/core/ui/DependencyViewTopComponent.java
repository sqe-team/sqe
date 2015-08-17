/* Copyright 2005,2006,2015 Sven Reimers, Florian Vogler
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
package org.nbheaven.sqe.tools.depfinder.dependencies.core.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.model.DependencyNode;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.model.ModelManager;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.resources.ResourcesConsts;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.DependencyScene;
import static org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.DependencyScene.*;
import org.nbheaven.sqe.core.ui.components.filter.FilterChangedListener;
import org.nbheaven.sqe.core.ui.components.filter.FilterType;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
public final class DependencyViewTopComponent extends TopComponent implements PropertyChangeListener, FilterChangedListener {

    /** path to the icon used by the component and its open action */
    private static final String PREFERRED_ID = "DependencyViewTopComponent";
    private final Project project;
    private DependencyScene scene;
    private JScrollPane scrollPane = null;

    public DependencyViewTopComponent(Project project, Node... context) {
        this.project = project;
        initComponents();
        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        setName(NbBundle.getMessage(DependencyViewTopComponent.class, "CTL_DependencyViewTopComponent", projectName));
        setToolTipText(NbBundle.getMessage(DependencyViewTopComponent.class, "HINT_DependencyViewTopComponent", projectName));
        setIcon(ImageUtilities.loadImage(ResourcesConsts.DEPFINDER_ICON_PATH, true));

        setUIEnabled(false);
    }

    public void filterChanged() {
        scene.filterChanged(jFilter);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName()) &&
                !Arrays.asList(OpenProjects.getDefault().getOpenProjects()).contains(project)) {
            close();
        }
    }
    private volatile boolean working = false;

    private boolean isWorking() {
        return working;
    }

    private void setWorking(boolean working) {
        this.working = working;
        setUIEnabled(!working);
    }

    private void setUIEnabled(boolean enable) {
        showExternalScopeButton.setEnabled(enable);
        showJDKScopeButton.setEnabled(enable);
        zoomToViewButton.setEnabled(enable);
        defaultZoomButton.setEnabled(enable);
        refreshButton.setEnabled(enable);
        jFilter.setVisible(enable);
    }

    private void setScene(DependencyScene scene) {
        if (this.scene != scene) {
            if (null != this.scene) {
                setUIEnabled(false);
                remove(scrollPane);
            }
            this.scene = scene;
            if (null != this.scene) {
                scrollPane = new JScrollPane(this.scene.createView());
                add(scrollPane, java.awt.BorderLayout.CENTER);
                showExternalScopeButton.setSelected(scene.isShowJdkScope());
                showJDKScopeButton.setSelected(scene.isShowJdkScope());
                setUIEnabled(true);
            }
        }
    }

    private void refreshScene(final boolean refreshModel) {
        setWorking(true);
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                final DependencyScene scene = new DependencyScene(ModelManager.getDefault().getModelRef(project));
                if (refreshModel) {
                    scene.getModel().update();
                }
                ProgressHandle progressHandle  = ProgressHandle.createHandle("DependencyFinder - " + ProjectUtils.getInformation(project).getDisplayName());                
                progressHandle.start(scene.getModel().getPackages().size());
                int count = 0;
                for (String packageName : scene.getModel().getPackages()) {
                    packageName = encodePackageName(packageName);
                    if (!scene.isNode(encodePackageName(packageName))) {
                        scene.addNode(packageName);
                    }
                    for (String usedPackageName : scene.getModel().getOutboundPackageDependencies(packageName)) {
                        usedPackageName = encodePackageName(usedPackageName);
                        if (!scene.isNode(usedPackageName)) {
                            scene.addNode(usedPackageName);
                        }
                        DependencyNode dependencyNode = new DependencyNode(scene.getModelRef(), packageName, usedPackageName);
                        if (packageName != usedPackageName && !packageName.equals(usedPackageName) && !scene.isEdge(dependencyNode)) {
                            scene.addEdge(dependencyNode);
                            scene.setEdgeSource(dependencyNode, packageName);
                            scene.setEdgeTarget(dependencyNode, usedPackageName);

                        }
                    }
                    progressHandle.progress(++count);
                }
                progressHandle.finish();
                scene.doSceneLayout();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        setScene(scene);
                        setWorking(false);
                    }
                });
            }
        });
//        Collection<String> nodes = new ArrayList<String>(scene.getNodes());
//        for (String node : nodes) {
//            scene.removeNodeWithEdges(node);
//        }
//        RequestProcessor.getDefault().post(new Runnable() {
//
//            public void run() {
//                if (refreshModel) {
//                    scene.getModel().update();
//                }
//                SwingUtilities.invokeLater(new Runnable() {
//
//                    public void run() {
//                        for (String packageName : scene.getModel().getPackages()) {
//                            packageName = encodePackageName(packageName);
//                            if (!scene.isNode(encodePackageName(packageName))) {
//                                scene.addNode(packageName);
//                            }
//                            for (String usedPackageName : getModel().getOutboundPackageDependencies(packageName)) {
//                                usedPackageName = encodePackageName(usedPackageName);
//                                createDependency(packageName, usedPackageName);
//                            }
//                        }
//                        sceneLayout.invokeLayout();
//                        getView().repaint();
//                    }
//                });
//            }
//        });
    }

    private void fitToView() {
        Rectangle rectangle = new Rectangle(0, 0, 1, 1);
        for (Widget widget : scene.getChildren()) {
            rectangle = rectangle.union(widget.convertLocalToScene(widget.getBounds()));
        }

        Dimension dim = rectangle.getSize();
        Dimension viewDim = scrollPane.getViewportBorderBounds().getSize();
        scene.setZoomFactor(Math.min((float) viewDim.width / dim.width, (float) viewDim.height / dim.height));
        scene.validate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        toolBarSeparator1 = new javax.swing.JToolBar.Separator();
        showJDKScopeButton = new javax.swing.JToggleButton();
        showExternalScopeButton = new javax.swing.JToggleButton();
        toolBarSeparator2 = new javax.swing.JToolBar.Separator();
        zoomToViewButton = new javax.swing.JButton();
        defaultZoomButton = new javax.swing.JButton();
        jFilter = new org.nbheaven.sqe.core.ui.components.filter.JFilter();

        setLayout(new java.awt.BorderLayout());

        toolBar.setRollover(true);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbheaven/sqe/tools/depfinder/dependencies/core/resources/refresh.png"))); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        toolBar.add(refreshButton);
        toolBar.add(toolBarSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(showJDKScopeButton, "JDK");
        showJDKScopeButton.setFocusable(false);
        showJDKScopeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        showJDKScopeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        showJDKScopeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showJDKScopeButtonActionPerformed(evt);
            }
        });
        toolBar.add(showJDKScopeButton);

        org.openide.awt.Mnemonics.setLocalizedText(showExternalScopeButton, "External");
        showExternalScopeButton.setFocusable(false);
        showExternalScopeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        showExternalScopeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        showExternalScopeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showExternalScopeButtonActionPerformed(evt);
            }
        });
        toolBar.add(showExternalScopeButton);
        toolBar.add(toolBarSeparator2);

        org.openide.awt.Mnemonics.setLocalizedText(zoomToViewButton, "ZoomToFit");
        zoomToViewButton.setFocusable(false);
        zoomToViewButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomToViewButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomToViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomToViewButtonActionPerformed(evt);
            }
        });
        toolBar.add(zoomToViewButton);

        org.openide.awt.Mnemonics.setLocalizedText(defaultZoomButton, "1:1");
        defaultZoomButton.setFocusable(false);
        defaultZoomButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        defaultZoomButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        defaultZoomButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultZoomButtonActionPerformed(evt);
            }
        });
        toolBar.add(defaultZoomButton);

        add(toolBar, java.awt.BorderLayout.NORTH);

        jFilter.addFilterItem(FilterType.STARTS_WITH, FilterType.CONTAINS, FilterType.REGEXP, FilterType.ENDS_WITH);
        jFilter.setDefaultFilterType(FilterType.STARTS_WITH);
        jFilter.addFilterListener(this);
        add(jFilter, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        refreshScene(true);
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void showJDKScopeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJDKScopeButtonActionPerformed
        scene.setShowJDKScope(showJDKScopeButton.isSelected());
}//GEN-LAST:event_showJDKScopeButtonActionPerformed

    private void showExternalScopeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showExternalScopeButtonActionPerformed
        scene.setShowExternalScope(showExternalScopeButton.isSelected());
}//GEN-LAST:event_showExternalScopeButtonActionPerformed

    private void zoomToViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomToViewButtonActionPerformed
        fitToView();
    }//GEN-LAST:event_zoomToViewButtonActionPerformed

    private void defaultZoomButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultZoomButtonActionPerformed
        scene.setZoomFactor(1);
        scene.validate();
    }//GEN-LAST:event_defaultZoomButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton defaultZoomButton;
    private org.nbheaven.sqe.core.ui.components.filter.JFilter jFilter;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToggleButton showExternalScopeButton;
    private javax.swing.JToggleButton showJDKScopeButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToolBar.Separator toolBarSeparator1;
    private javax.swing.JToolBar.Separator toolBarSeparator2;
    private javax.swing.JButton zoomToViewButton;
    // End of variables declaration//GEN-END:variables
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        OpenProjects.getDefault().addPropertyChangeListener(this);
        refreshScene(true);
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        OpenProjects.getDefault().removePropertyChangeListener(this);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
