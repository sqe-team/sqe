///* Copyright 2005,2006 Sven Reimers, Florian Vogler
// *
// * This file is part of the Software Quality Environment Project.
// *
// * The Software Quality Environment Project is free software:
// * you can redistribute it and/or modify it under the terms of the
// * GNU General Public License as published by the Free Software Foundation,
// * either version 2 of the License, or (at your option) any later version.
// *
// * The Software Quality Environment Project is distributed in the hope that
// * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
// * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// * See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
// */
//package org.nbheaven.sqe.codedefects.ui.actions;
//
//import java.awt.Component;
//import java.awt.event.ActionEvent;
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import javax.swing.AbstractAction;
//import javax.swing.Action;
//import javax.swing.ButtonModel;
//import javax.swing.JToggleButton;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
//import org.nbheaven.sqe.codedefects.core.api.QualityProvider.SessionEventProxy;
//import org.nbheaven.sqe.codedefects.core.api.QualitySession;
//import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
//import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
//import org.netbeans.api.annotations.common.NonNull;
//import org.netbeans.api.annotations.common.NullAllowed;
//import org.netbeans.api.project.Project;
//import org.openide.loaders.DataObject;
//import org.openide.util.ContextAwareAction;
//import org.openide.util.Lookup;
//import org.openide.util.Utilities;
//import org.openide.util.WeakListeners;
//import org.openide.util.actions.Presenter;
//
///**
// *
// * @author Sven Reimers
// */
//public abstract class AbstractShowAnnotationsAction extends AbstractAction implements ContextAwareAction, Presenter.Toolbar {
//
//    private final @NonNull
//    Lookup lookup;
//    private JToggleButton button;
//
//    /**
//     * @param lookup null for global instance, else a context
//     */
//    protected AbstractShowAnnotationsAction(@NullAllowed Lookup lookup) {
//        this.lookup = lookup != null ? lookup : Utilities.actionsGlobalContext();
//        assert this.lookup != null;
//    }
//
//    @Override
//    public final void actionPerformed(ActionEvent actionEvent) {
//        DataObject dataObject = lookup.lookup(DataObject.class);
//        QualitySession session = SQECodedefectSupport.retrieveSessionFromDataObject(dataObject, getQualityProvider());
//
//        boolean old = session.isAnnotateProjectResultEnabledProperty();
//        session.setAnnotateProjectResultEnabledProperty(!old);
//    }
//
//    @Override
//    public abstract Action createContextAwareInstance(Lookup lookup);
//
//    @Override
//    public final Component getToolbarPresenter() {
//        if (null == button) {
//            button = new MyToolbarToggleButton();
//            DataObject dataObject = lookup.lookup(DataObject.class);
//            Project project = SQEProjectSupport.findProject(dataObject);
//            button.setModel(new ToggleButtonModel(project, getQualityProvider()));
//            button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
//            button.setAction(this);
//            if (null == project) {
//                this.setEnabled(false);
//            }
//            return button;
//        }
//        return button;
//    }
//
//    protected abstract QualityProvider getQualityProvider();
//
//    private static final class ToggleButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener {
//
//        private final QualityProvider qualityProvider;
//        private final Project project;
//        private PropertyChangeListener weakListener;
//
//        public ToggleButtonModel(Project project, QualityProvider qualityProvider) {
//            this.project = project;
//            this.qualityProvider = qualityProvider;
//            if (null != this.project) {
//
//                weakListener = qualityProvider.getSessionEventProxy().addWeakPropertyChangeListener(SessionEventProxy.ANNOTATE_PROJECT_RESULT_ENABLED_PROPERTY, this);
//                propertyChange(null);
//            }
//        }
//
//        @Override
//        public void propertyChange(PropertyChangeEvent evt) {
//            if (evt == null || evt.getPropertyName() == null || evt.getPropertyName().equals(SQECodedefectProperties.getPropertyNameAnnotateActive(qualityProvider))) {
//                setSelected(SQECodedefectSupport.isQualityProviderAnnotateE(project, qualityProvider));
//            }
//        }
//    }
//
//    private static final class MyToolbarToggleButton extends JToggleButton implements ChangeListener {
//
//        public MyToolbarToggleButton() {
//        }
//
//        @Override
//        public void setModel(ButtonModel model) {
//            ButtonModel oldModel = getModel();
//            if (oldModel != null) {
//                oldModel.removeChangeListener(this);
//            }
//
//            super.setModel(model);
//
//            ButtonModel newModel = getModel();
//            if (newModel != null) {
//                newModel.addChangeListener(this);
//            }
//
//            stateChanged(null);
//        }
//
//        @Override
//        public void stateChanged(ChangeEvent evt) {
//            boolean selected = isSelected();
//            super.setContentAreaFilled(selected);
//            super.setBorderPainted(selected);
//        }
//
//        @Override
//        public void setBorderPainted(boolean arg0) {
//            if (!isSelected()) {
//                super.setBorderPainted(arg0);
//            }
//        }
//
//        @Override
//        public void setContentAreaFilled(boolean arg0) {
//            if (!isSelected()) {
//                super.setContentAreaFilled(arg0);
//            }
//        }
//    }
//}
