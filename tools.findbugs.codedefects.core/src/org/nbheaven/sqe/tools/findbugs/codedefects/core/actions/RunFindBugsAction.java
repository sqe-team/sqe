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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsQualityProvider;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.ui.FindBugsTopComponent;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.core.api.SQEManager;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action to trigger findbugs run on actual project
 * @author Florian Vogler
 */
@ActionID(id = "org.nbheaven.sqe.tools.findbugs.codedefects.core.actions.RunFindBugsAction", category = "Quality")
@ActionRegistration(displayName = "#LBL_RunFindBugsAction", lazy = true)
@ActionReferences({
    @ActionReference(path = "Menu/Quality/CodeDefects", name = "RunFindBugsAction", position = 200),
    @ActionReference(path = "SQE/Projects/Actions/CodeDefects", name = "RunFindBugsAction", position = 200)
})
public class RunFindBugsAction extends AbstractAction implements LookupListener, ContextAwareAction, PropertyChangeListener {

    private Lookup context;
    private Lookup.Result<Project> lkpInfo;

    public RunFindBugsAction() {
        this(Utilities.actionsGlobalContext());
    }

    private RunFindBugsAction(Lookup context) {
        //        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(Action.NAME, NbBundle.getMessage(RunFindBugsAction.class, "LBL_RunFindBugsAction")); //NOI18N
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/findbugs.png")));
        this.context = context;
    }

    public Action createContextAwareInstance(Lookup context) {
        return new RunFindBugsAction(context);
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
        SQECodedefectProperties.addPropertyChangeListener(SQECodedefectProperties.getPropertyNameActive(FindBugsQualityProvider.getDefault()), this);//TODO Make weak !!!


        //The thing we want to listen for the presence or absence of
        //on the global selection
        Lookup.Template<Project> tpl = new Lookup.Template<Project>(Project.class);
        lkpInfo = context.lookup(tpl);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    public void resultChanged(LookupEvent ev) {
        updateEnableState();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SQECodedefectProperties.getPropertyNameActive(FindBugsQualityProvider.getDefault()))) {
            updateEnableState();
        }
    }

    private void updateEnableState() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    updateEnableState();
                }
            });
            return;
        }
        setEnabled(isEnabled(getActiveProject()));
    }

    private Project getActiveProject() {
        Collection<? extends Project> projects = lkpInfo.allInstances();
        if (projects.size() == 1) {
            Project project = projects.iterator().next();
            return project;
        }
        return null;
    }

    public void actionPerformed(ActionEvent e) {
        Project project = getActiveProject();
        if (null != project) {
            if (isEnabled(project)) {
                FindBugsSession session = getFindBugsSession(project);
                session.computeResult();

                SQEManager.getDefault().setActiveProject(project);
                FindBugsTopComponent tc = FindBugsTopComponent.findInstance();
                tc.open();
            }
        }
    }

    private FindBugsSession getFindBugsSession(Project project) {
        return project.getLookup().lookup(FindBugsSession.class);
    }

    private boolean isQualityProviderActive(Project project) {
        return SQECodedefectProperties.isQualityProviderActive(project, FindBugsQualityProvider.getDefault());
    }

    private boolean isValidForProject(Project project) {
        return FindBugsQualityProvider.getDefault().isValidFor(project);
    }

    private boolean isEnabled(Project project) {
        return null != project && isValidForProject(project) && isQualityProviderActive(project) && null != getFindBugsSession(project);
    }
}
