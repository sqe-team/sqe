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
package org.nbheaven.sqe.tools.pmd.codedefects.core.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDQualityProvider;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDSession;
import org.nbheaven.sqe.tools.pmd.codedefects.core.ui.PMDTopComponent;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.nbheaven.sqe.core.api.SQEManager;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action to trigger pmd run on actual project
 *
 * @author Florian Vogler
 */
public class RunPMDAction extends AbstractAction implements LookupListener, ContextAwareAction, PropertyChangeListener {

    private Lookup context;
    private Lookup.Result<Node> lkpInfo;

    public RunPMDAction() {
        this(Utilities.actionsGlobalContext());
    }

    private RunPMDAction(Lookup context) {
        //        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(Action.NAME, NbBundle.getMessage(RunPMDAction.class, "LBL_RunPMDAction")); //NOI18N
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/pmd/codedefects/core/resources/pmd.png")));
        this.context = context;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new RunPMDAction(context);
    }

    @Override
    public boolean isEnabled() {
        init();
        return super.isEnabled();
    }

    private void init() {
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        if (lkpInfo != null) {
            return;
        }
        SQECodedefectProperties.addPropertyChangeListener(SQECodedefectProperties.getPropertyNameActive(PMDQualityProvider.getDefault()), this);//TODO Make weak !!!

        //The thing we want to listen for the presence or absence of
        //on the global selection
        Lookup.Template<Node> tpl = new Lookup.Template<>(Node.class);
        lkpInfo = context.lookup(tpl);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        updateEnableState();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SQECodedefectProperties.getPropertyNameActive(PMDQualityProvider.getDefault()))) {
            updateEnableState();
        }
    }

    private void updateEnableState() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this::updateEnableState);
        } else {
            setEnabled(isEnabled(getActiveProject()));
        }
    }

    private Project getActiveProject() {
        Collection<? extends Node> nodes = lkpInfo.allInstances();
        if (nodes.size() == 1) {
            Project project = SQEProjectSupport.findProject(nodes.iterator().next());
            return project;
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = getActiveProject();
        if (null != project) {
            if (isEnabled(project)) {
                PMDSession session = SQECodedefectSupport.retrieveSession(project, PMDSession.class);
                session.computeResult();

                SQEManager.getDefault().setActiveProject(project);
                PMDTopComponent tc = PMDTopComponent.findInstance();
                tc.open();
            }
        }
    }

    private boolean isEnabled(Project project) {
        return SQECodedefectSupport.isQualityProviderActive(project, PMDSession.class)
                && PMDQualityProvider.getDefault().isValidFor(project);
    }
}
