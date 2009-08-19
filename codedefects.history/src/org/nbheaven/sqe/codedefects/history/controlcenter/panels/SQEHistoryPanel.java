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
package org.nbheaven.sqe.codedefects.history.controlcenter.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JButton;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.GroupLayout.ParallelGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.text.GapContent;
import org.jdesktop.layout.GroupLayout.SequentialGroup;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.codedefects.core.spi.SQEUtilities;
import org.nbheaven.sqe.codedefects.history.History;
import org.nbheaven.sqe.core.api.SQEManager;
import org.netbeans.api.project.Project;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sven Reimers
 */
public class SQEHistoryPanel extends javax.swing.JPanel implements PropertyChangeListener {

    private History activeHistory;
    private final DefaultTableXYDataset perProjectDataSet = new DefaultTableXYDataset();
    private JFreeChart historyChart; 
    private JButton clearHistoryButton;

    /** Creates new form SQEHistoryPanel */
    public SQEHistoryPanel() {                       
        historyChart = org.jfree.chart.ChartFactory.createStackedXYAreaChart(null, "Snapshot", "CodeDefects", perProjectDataSet, PlotOrientation.VERTICAL, false, true, false);
        historyChart.setBackgroundPaint(Color.WHITE);
        historyChart.getXYPlot().setRangeGridlinePaint(Color.BLACK);
        historyChart.getXYPlot().setDomainGridlinePaint(Color.BLACK);
        historyChart.getXYPlot().setBackgroundPaint(Color.WHITE);
        
        XYPlot plot = historyChart.getXYPlot();
        plot.setForegroundAlpha(0.7f);
//        plot.getRenderer();
        
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();            
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        LogarithmicAxis rangeAxis = new LogarithmicAxis("CodeDefects");
        rangeAxis.setStrictValuesFlag(false);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setRangeAxis(rangeAxis);        
        
        StackedXYAreaRenderer2 categoryItemRenderer = new StackedXYAreaRenderer2(); //3D();
        categoryItemRenderer.setSeriesPaint(0, Color.RED);
        categoryItemRenderer.setSeriesPaint(1, Color.ORANGE);
        categoryItemRenderer.setSeriesPaint(2, Color.YELLOW);        
        
        plot.setRenderer(categoryItemRenderer);        
        
        ChartPanel historyChartPanel = new ChartPanel(historyChart);
        historyChartPanel.setBorder(null);
        historyChartPanel.setPreferredSize(new Dimension(150,200));
        historyChartPanel.setBackground(Color.WHITE);
        initComponents();        

        historyView.setLayout(new BorderLayout());
        historyView.add(historyChartPanel, BorderLayout.CENTER);

        JPanel selectorPanel = new JPanel();
        selectorPanel.setOpaque(false);

        GroupLayout layout = new GroupLayout(selectorPanel);
        selectorPanel.setLayout(layout);

        // Turn on automatically adding gaps between components
        layout.setAutocreateGaps(true);

        // Turn on automatically creating gaps between components that touch
        // the edge of the container and the container.
        layout.setAutocreateContainerGaps(true);
        
        ParallelGroup horizontalParallelGroup = layout.createParallelGroup(GroupLayout.LEADING);
        SequentialGroup verticalSequentialGroup = layout.createSequentialGroup();

        layout.setHorizontalGroup(
           layout.createSequentialGroup()
              .add(horizontalParallelGroup)
        );
        
        layout.setVerticalGroup(verticalSequentialGroup);

        clearHistoryButton = new JButton();
        clearHistoryButton.setEnabled(false);
        clearHistoryButton.setIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/codedefects/history/resources/trash.png")));
        clearHistoryButton.setOpaque(false);
        clearHistoryButton.setFocusPainted(false);
        clearHistoryButton.setToolTipText(NbBundle.getBundle("org/nbheaven/sqe/codedefects/history/controlcenter/panels/Bundle").getString("HINT_clear_button"));
        horizontalParallelGroup.add(clearHistoryButton);
        verticalSequentialGroup.add(clearHistoryButton);
        clearHistoryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (null != activeHistory) {
                    activeHistory.clear();
                }
            }

        });

        Component createVerticalStrut = Box.createVerticalStrut(10);

        horizontalParallelGroup.add(createVerticalStrut);
        verticalSequentialGroup.add(createVerticalStrut);

        for (final QualityProvider provider : SQEUtilities.getProviders()) {
            final JToggleButton providerButton = new JToggleButton();
            providerButton.setIcon(provider.getIcon());
            providerButton.setOpaque(false);
            providerButton.setFocusPainted(false);
            horizontalParallelGroup.add(providerButton);
            verticalSequentialGroup.add(providerButton);
            ActionListener listener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (providerButton.isSelected()) {
                        addSelectedProvider(provider);
                    } else {
                        removeSelectedProvider(provider);
                    }
                    updateView();
                }
            };
            providerButton.addActionListener(listener);
            addSelectedProvider(provider);
            providerButton.setSelected(true);
        }

        historyView.add(selectorPanel, BorderLayout.EAST);
    }


    private String[] providers = new String[] {};

    private String[] getSelectedProviders() {
        return providers;
    }

    private void setSelectedProviders(String[] providers) {
        this.providers = providers;
    }

    private void addSelectedProvider(QualityProvider provider) {
        Set<String> l = new HashSet<String>(Arrays.asList(getSelectedProviders()));
        l.add(provider.getId());
        setSelectedProviders(l.toArray(new String[l.size()]));
    }

    private void removeSelectedProvider(QualityProvider provider) {
        Set<String> l = new HashSet<String>(Arrays.asList(getSelectedProviders()));
        l.remove(provider.getId());
        setSelectedProviders(l.toArray(new String[l.size()]));
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
        perProjectDataSet.removeAllSeries();
        XYSeries error = new XYSeries("Error", true, false);
        XYSeries warning = new XYSeries("Warning", true, false);
        XYSeries info = new XYSeries("Info", true, false);

        if (null != activeHistory) {
            int i = 1;
            for (History.Entry entry: activeHistory) {
                QualityResultStatistic result = entry.get(getSelectedProviders());
                error.add(new XYDataItem(i, result.getCodeDefactCount(CodeDefectSeverity.ERROR)));
                warning.add(new XYDataItem(i, result.getCodeDefactCount(CodeDefectSeverity.WARNING)));
                info.add(new XYDataItem(i, result.getCodeDefactCount(CodeDefectSeverity.INFO)));
                i++;
             }
            clearHistoryButton.setEnabled(!activeHistory.isEmpty());
        }

        perProjectDataSet.addSeries(error);
        perProjectDataSet.addSeries(warning);
        perProjectDataSet.addSeries(info);


        invalidate();
        revalidate();
        repaint();
    }
    
    private void setActiveProject(Project project) {
        if (null != activeHistory) {
            activeHistory.removePropertyChangeListener(History.PROP_HISTORY_CHANGED, this);
        }
        if (null != project) {
            activeHistory = History.getHistory(project);
            activeHistory.addPropertyChangeListener(History.PROP_HISTORY_CHANGED, this);
        }
        else {
            activeHistory = null;
        }
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
        setActiveProject(null);
        super.removeNotify();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        historyView = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));

        historyView.setBorder(null);
        historyView.setOpaque(false);

        org.jdesktop.layout.GroupLayout historyViewLayout = new org.jdesktop.layout.GroupLayout(historyView);
        historyView.setLayout(historyViewLayout);
        historyViewLayout.setHorizontalGroup(
            historyViewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 305, Short.MAX_VALUE)
        );
        historyViewLayout.setVerticalGroup(
            historyViewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 108, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(historyView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(historyView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel historyView;
    // End of variables declaration//GEN-END:variables

}
