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
package org.nbheaven.sqe.tools.pmd.codedefects.core.option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;

/**
 * Panel for PMD rules
 * @author  antoni
 */
public class ConfigureRulesPanel extends javax.swing.JPanel {

    private static final Logger logger = Logger.getLogger(ConfigureRulesPanel.class.getPackage().getName());
    private PMDSettings pmdSettings;

    /** Creates new form ConfigureRulesPanel */
    public ConfigureRulesPanel() {
        initComponents();
    }

    /** Creates new form ConfigureRulesPanel */
    @Deprecated
    public ConfigureRulesPanel(PMDSettings pmdSettings) {
        initComponents();
        setSettings(pmdSettings);
    }

    public void setSettings(PMDSettings pmdSettings) {
        this.pmdSettings = pmdSettings;
        fillTable();
    }

    private void fillTable() {
        if (null == pmdSettings) {
            return;
        }
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(new Object[][]{},
                new String[]{"Rule", "Enabled"}) {

            Class[] types = new Class[]{java.lang.String.class, java.lang.Boolean.class};
            boolean[] canEdit = new boolean[]{false, true};

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

        Collection<Rule> rules = new ArrayList<Rule>();
        RuleSetFactory ruleSetFactory = new RuleSetFactory();
        try {
            Iterator<RuleSet> iterator = ruleSetFactory.getRegisteredRuleSets();

            while (iterator.hasNext()) {
                RuleSet ruleSet = iterator.next();
                rules.addAll(ruleSet.getRules());
            }

            for (Rule rule : rules) {
                //if rule is not in preferences then assume it is ON
                model.addRow(new Object[]{rule, pmdSettings.isRuleActive(rule)});
            }
            SortedRuleTableModel sorter = new SortedRuleTableModel(model, rulesTable.getTableHeader());
            rulesTable.setModel(sorter);
            rulesTable.getColumnModel().getColumn(0).setCellRenderer(new RuleCellRenderer());
            rulesTable.getSelectionModel().addListSelectionListener(new RulesListListener());

        } catch (RuleSetNotFoundException ex) {
            logger.log(Level.SEVERE, "exception", ex);
        }

    }

    public void saveSettingsToPreferences() {
        Rule rule = null;
        Boolean enabled = true;
        SortedRuleTableModel model = (SortedRuleTableModel) rulesTable.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            rule = (Rule) model.getValueAt(i, 0);
            enabled = (Boolean) model.getValueAt(i, 1);
            if (enabled) {
                pmdSettings.activateRule(rule);
            } else {
                pmdSettings.deactivateRule(rule);
            }
        }
    }

    private class RulesListListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {

            if (e.getValueIsAdjusting()) {
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            int selectedRow = lsm.getMinSelectionIndex();

            if (selectedRow >= 0) {

                SortedRuleTableModel model = (SortedRuleTableModel) rulesTable.getModel();
                Rule rule = (Rule) model.getValueAt(selectedRow, 0);
                informationArea.setText(rule.getMessage());

                StringBuilder stringBuilder = new StringBuilder();
                for (String string : rule.getExamples()) {
                    stringBuilder.append(string);
                }
                exampleEditor.setText(stringBuilder.toString());
            }

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        informationArea = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        exampleEditor = new javax.swing.JEditorPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        rulesTable = new javax.swing.JTable();

        informationArea.setColumns(20);
        informationArea.setRows(5);
        jScrollPane2.setViewportView(informationArea);

        exampleEditor.setContentType(org.openide.util.NbBundle.getMessage(ConfigureRulesPanel.class, "ConfigureRulesPanel.exampleEditor.contentType")); // NOI18N
        jScrollPane3.setViewportView(exampleEditor);

        rulesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Rule", "Enabled"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        fillTable();
        jScrollPane1.setViewportView(rulesTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 151, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(26, 26, 26)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 204, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane exampleEditor;
    private javax.swing.JTextArea informationArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable rulesTable;
    // End of variables declaration//GEN-END:variables
}
