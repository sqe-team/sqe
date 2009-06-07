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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleQualityProvider;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleSession;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.ui.CheckstyleTopComponent;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.core.api.SQEManager;
import org.netbeans.api.project.Project;
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
public class RunCheckstyleAction extends AbstractAction implements LookupListener, ContextAwareAction, PropertyChangeListener {

    private Lookup context;
    private Lookup.Result<Project> lkpInfo;

    public RunCheckstyleAction() {
        this(Utilities.actionsGlobalContext());
    }

    private RunCheckstyleAction(Lookup context) {
        //        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(Action.NAME, NbBundle.getMessage(RunCheckstyleAction.class, "LBL_CheckstyleAction")); //NOI18N
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/checkstyle.png")));
        this.context = context;
    }

    public Action createContextAwareInstance(Lookup context) {
        return new RunCheckstyleAction(context);
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
        SQECodedefectProperties.addPropertyChangeListener(SQECodedefectProperties.getPropertyNameActive(CheckstyleQualityProvider.getDefault()), this);//TODO Make weak !!!


        //The thing we want to listen for the presence or absence of
        //on the global selection
        Lookup.Template<Project> tpl = new Lookup.Template<Project>(Project.class);
        lkpInfo = context.lookup(tpl);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    public void resultChanged(final LookupEvent ev) {
        updateEnableState();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SQECodedefectProperties.getPropertyNameActive(CheckstyleQualityProvider.getDefault()))) {
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
                CheckstyleSession session = getCheckstyleSession(project);
                session.computeResult();

                SQEManager.getDefault().setActiveProject(project);
                CheckstyleTopComponent tc = CheckstyleTopComponent.findInstance();
                tc.open();
            }
        }
    }

    private CheckstyleSession getCheckstyleSession(Project project) {
        return project.getLookup().lookup(CheckstyleSession.class);
    }

    private boolean isQualityProviderActive(Project project) {
        return SQECodedefectProperties.isQualityProviderActive(project, CheckstyleQualityProvider.getDefault());
    }

    private boolean isValidForProject(Project project) {
        return CheckstyleQualityProvider.getDefault().isValidFor(project);
    }

    private boolean isEnabled(Project project) {
        return null != project && isValidForProject(project) && isQualityProviderActive(project) && null != getCheckstyleSession(project);
    }
}
