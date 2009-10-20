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
package org.nbheaven.sqe.codedefects.ui.panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.spi.SQEUtilities;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.core.api.SQEManager;
import org.netbeans.api.project.Project;


/**
 *
 * @author Sven Reimers
 */
public class DefectProviderConfigurationPanel extends JPanel {

    public DefectProviderConfigurationPanel() {
        init();
    }
    
    private void init() {
        this.setBackground(Color.WHITE);
        this.setOpaque(true);
        for (final QualityProvider provider : SQEUtilities.getProviders()) {
            CodeDefectProviderSelector checkbox = new CodeDefectProviderSelector(provider);
            checkbox.setProject(null);
            this.add(checkbox);
        }
    }
    
    private class CodeDefectProviderSelector extends JCheckBox implements PropertyChangeListener, ActionListener {
        
        private QualityProvider provider;
        private Project project;

        public CodeDefectProviderSelector(QualityProvider provider) {
            this.provider = provider;
            this.setText(provider.getDisplayName());
            this.addActionListener(this);
            this.setOpaque(true);
            this.setBackground(Color.WHITE);
        }
        
        public void actionPerformed(ActionEvent e) {
            SQECodedefectProperties.setQualityProviderActive(project, provider, this.isSelected());
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            setProject(SQEManager.getDefault().getActiveProject());
        }

        private void setProject(Project project) {
            this.project = project;
            if (null != project) {
                this.setSelected(SQECodedefectProperties.isQualityProviderActive(project, provider));
                this.setEnabled(true);
            } else {
                setSelected(false);
                setEnabled(false);
            }
        }
        
        @Override
        public void addNotify() {
            super.addNotify();
            SQEManager.getDefault().addPropertyChangeListener(SQEManager.PROP_ACTIVE_PROJECT, this);
        }

        @Override
        public void removeNotify() {
            SQEManager.getDefault().removePropertyChangeListener(SQEManager.PROP_ACTIVE_PROJECT, this);
            super.removeNotify();
        }
        
        
        
        
    }
}
