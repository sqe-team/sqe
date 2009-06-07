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
package org.nbheaven.sqe.codedefects.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;


/**
 *
 * @author Sven Reimers
 */
public abstract class AbstractShowAnnotationsAction extends AbstractAction implements ContextAwareAction, Presenter.Toolbar {

    private Lookup lookup;
    private JToggleButton button; 
    
    public AbstractShowAnnotationsAction(Lookup lookup) {
        this.lookup = lookup;
    }
    
    public final void actionPerformed(ActionEvent actionEvent) {
        Lookup lkp;
        if (lookup == Lookup.EMPTY) {
            lkp = Utilities.actionsGlobalContext();
        } else {
            lkp = this.lookup;
        }

        DataObject dataObject = lkp.lookup(DataObject.class);
        Project project = SQEProjectSupport.findProject(dataObject);
        boolean old = SQECodedefectProperties.isQualityProviderAnnotateActive(project, getQualityProvider());
        SQECodedefectProperties.setQualityProviderAnnotateActive(project, getQualityProvider(), !old);   
    }

    public abstract Action createContextAwareInstance(Lookup lookup);

    public final Component getToolbarPresenter() {
        if (null == button) {
            button = new MyToolbarToggleButton();
            DataObject dataObject = lookup.lookup(DataObject.class);
            Project project = SQEProjectSupport.findProject(dataObject);
            button.setModel(new ToggleButtonModel(project, getQualityProvider()));
            button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            button.setAction(this);            
            if (null == project) {
                this.setEnabled(false);
            }
            return button;            
        }
        return button;
    }
    
    protected abstract QualityProvider getQualityProvider();
    
    private static final class ToggleButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener {

        private final QualityProvider qualityProvider;
        private final Project project;
        
        public ToggleButtonModel(Project project, QualityProvider qualityProvider) {
            this.project = project;
            this.qualityProvider = qualityProvider;
            if (null != this.project) {
                SQECodedefectProperties.addPropertyChangeListener(WeakListeners.propertyChange(this, SQECodedefectProperties.class));
                propertyChange(null);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt == null || evt.getPropertyName() == null || evt.getPropertyName().equals(SQECodedefectProperties.getPropertyNameAnnotateActive(qualityProvider))) {
                setSelected(SQECodedefectProperties.isQualityProviderAnnotateActive(project, qualityProvider));
            }
        }
    }
    
    private static final class MyToolbarToggleButton extends JToggleButton implements ChangeListener {

        public MyToolbarToggleButton() {
        }

        @Override
        public void setModel(ButtonModel model) {
            ButtonModel oldModel = getModel();
            if (oldModel != null) {
                oldModel.removeChangeListener(this);
            }

            super.setModel(model);

            ButtonModel newModel = getModel();
            if (newModel != null) {
                newModel.addChangeListener(this);
            }

            stateChanged(null);
        }

        public void stateChanged(ChangeEvent evt) {
            boolean selected = isSelected();
            super.setContentAreaFilled(selected);
            super.setBorderPainted(selected);
        }

        @Override
        public void setBorderPainted(boolean arg0) {
            if (!isSelected()) {
                super.setBorderPainted(arg0);
            }
        }

        @Override
        public void setContentAreaFilled(boolean arg0) {
            if (!isSelected()) {
                super.setContentAreaFilled(arg0);
            }
        }
    }
}
