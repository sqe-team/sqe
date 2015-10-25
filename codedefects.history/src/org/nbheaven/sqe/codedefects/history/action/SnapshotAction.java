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
package org.nbheaven.sqe.codedefects.history.action;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.nbheaven.sqe.codedefects.history.util.CodeDefectHistoryPersistence;
import org.netbeans.api.project.Project;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Sven Reimers
 */
public class SnapshotAction extends AbstractAction implements LookupListener, ContextAwareAction {

    private Lookup context;
    private Lookup.Result<Project> lkpInfo;

    public SnapshotAction() {
        this(Utilities.actionsGlobalContext());
    }

    public SnapshotAction(Lookup context) {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(SnapshotAction.class, "HINT_Action"));
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/codedefects/history/resources/camera.png")));
        this.context = context;
        //The thing we want to listen for the presence or absence of
        //on the global selection
        Lookup.Template<Project> tpl = new Lookup.Template<Project>(Project.class);
        lkpInfo = context.lookup(tpl);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new SnapshotAction(context);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        updateEnableState();
    }

    public String getName() {
        return NbBundle.getMessage(SnapshotAction.class, "LBL_Action");
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (null != getActiveProject()) {
            Project project = getActiveProject();
            CodeDefectHistoryPersistence.addSnapshot(project);
        }
    }

    private void updateEnableState() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(() -> updateEnableState());
            return;
        }

        setEnabled(SQECodedefectSupport.isQualityAwareProject(getActiveProject()));
    }

    private Project getActiveProject() {
        Collection<? extends Project> projects = lkpInfo.allInstances();
        if (projects.size() == 1) {
            Project project = projects.iterator().next();
            return project;
        }
        return null;
    }

}
