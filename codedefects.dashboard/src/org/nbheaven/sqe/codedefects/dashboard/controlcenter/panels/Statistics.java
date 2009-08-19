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
package org.nbheaven.sqe.codedefects.dashboard.controlcenter.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.spi.SQEUtilities;
import org.nbheaven.sqe.core.api.SQEManager;
import org.netbeans.api.project.Project;

/**
 *
 * @author  Sven Reimers
 */
public class Statistics extends JPanel implements PropertyChangeListener {

    private Project activeProject;
    private final DefaultCategoryDataset perProjectDataSet = new DefaultCategoryDataset();
    private ChartPanel perProjectOverviewPanel;

    /** Creates new form Statistics */
    public Statistics() {
        initComponents();
        initComponentsManual();
        updateView();
    }

    private void initComponentsManual() {
        JFreeChart perProjectOverview = createOverviewPanel(perProjectDataSet);
        perProjectOverviewPanel = new ChartPanel(perProjectOverview);
        perProjectOverviewPanel.setPreferredSize(new Dimension(0, 150));
        this.add(perProjectOverviewPanel, BorderLayout.CENTER);
    }

    private static JFreeChart createOverviewPanel(DefaultCategoryDataset dataSet) {

        JFreeChart overview = org.jfree.chart.ChartFactory.createStackedBarChart(null, null, "CodeDefects", dataSet, PlotOrientation.HORIZONTAL, false, true, false);
        overview.setBorderVisible(false);
        overview.setBackgroundPaint(Color.WHITE);
        overview.setAntiAlias(true);
        overview.setNotify(true);

        CategoryPlot overviewPlot = overview.getCategoryPlot();
        overviewPlot.setRangeGridlinePaint(Color.BLACK);
        overviewPlot.setDomainGridlinePaint(Color.BLACK);
        overviewPlot.setBackgroundPaint(Color.WHITE);
        overviewPlot.setForegroundAlpha(0.7f);
        overviewPlot.setRangeAxisLocation(AxisLocation.getOpposite(overviewPlot.getRangeAxisLocation()));

        CategoryAxis domainAxis = overviewPlot.getDomainAxis();
        domainAxis.setVisible(true);

        LogarithmicAxis rangeAxis = new LogarithmicAxis("CodeDefects");
        rangeAxis.setLabel(null);
        rangeAxis.setStrictValuesFlag(false);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        overviewPlot.setRangeAxis(rangeAxis);

        CategoryItemRenderer categoryItemRenderer = new StackedBarRenderer(); //3D();
        categoryItemRenderer.setItemLabelsVisible(true);
//        categoryItemRenderers[0].setPaint(Color.RED);
        categoryItemRenderer.setSeriesPaint(0, Color.RED);
        categoryItemRenderer.setSeriesPaint(1, Color.ORANGE);
        categoryItemRenderer.setSeriesPaint(2, Color.YELLOW);

        categoryItemRenderer.setBaseItemLabelsVisible(true);

        overviewPlot.setRenderer(categoryItemRenderer);

        return overview;
    }

    private void updateView() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    updateView();
                }
            });
            return;
        }

        assert SwingUtilities.isEventDispatchThread();

        // Update per project view first
        perProjectDataSet.clear();
        for (QualityProvider qualityProvider : SQEUtilities.getProviders()) {
            String providerName = qualityProvider.getName();
            perProjectDataSet.addValue(0, CodeDefectSeverity.ERROR, providerName);
            perProjectDataSet.addValue(0, CodeDefectSeverity.WARNING, providerName);
            perProjectDataSet.addValue(0, CodeDefectSeverity.INFO, providerName);
        }
        
        if (null != activeProject) {
            Collection<? extends QualitySession> sessions = activeProject.getLookup().lookupAll(QualitySession.class);
            for (QualitySession session : sessions) {
                if (null == session.getResult()) {
                    continue;
                }
                QualityResultStatistic statistic = (QualityResultStatistic) session.getResult().getLookup().lookup(QualityResultStatistic.class);
                if (null != statistic) {
                    String providerName = session.getProvider().getName();
                    perProjectDataSet.addValue(statistic.getCodeDefactCount(CodeDefectSeverity.ERROR), CodeDefectSeverity.ERROR, providerName);
                    perProjectDataSet.addValue(statistic.getCodeDefactCount(CodeDefectSeverity.WARNING), CodeDefectSeverity.WARNING, providerName);
                    perProjectDataSet.addValue(statistic.getCodeDefactCount(CodeDefectSeverity.INFO), CodeDefectSeverity.INFO, providerName);
                }
            }
        }
        Statistics.this.invalidate();
        Statistics.this.revalidate();
        Statistics.this.repaint();
    }

    private void setActiveProject(Project project) {
        if (null != activeProject) {
            for (QualitySession session : activeProject.getLookup().lookupAll(QualitySession.class)) {
                session.removePropertyChangeListener(QualitySession.RESULT, this);
            }
        }
        if (null != project) {
            for (QualitySession session : project.getLookup().lookupAll(QualitySession.class)) {
                session.addPropertyChangeListener(QualitySession.RESULT, this);
            }
        }
        activeProject = project;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (SQEManager.PROP_ACTIVE_PROJECT.equals(evt.getPropertyName())) {
            setActiveProject((Project) evt.getNewValue());
        }
        updateView();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setActiveProject(SQEManager.getDefault().getActiveProject());
        SQEManager.getDefault().addPropertyChangeListener(SQEManager.PROP_ACTIVE_PROJECT, this);
        updateView();
    }

    @Override
    public void removeNotify() {
        SQEManager.getDefault().removePropertyChangeListener(SQEManager.PROP_ACTIVE_PROJECT, this);
        super.removeNotify();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
