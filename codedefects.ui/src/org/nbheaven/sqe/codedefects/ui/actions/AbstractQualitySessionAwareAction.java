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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider.SessionEventProxy;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * A context aware action sensitive to session properties
 *
 * @author Florian Vogler
 */
public abstract class AbstractQualitySessionAwareAction extends AbstractAction {

    private final Lookup context;
    private final Lookup.Result<Node> lkpInfo;
    private final PropertyChangeListener[] weakListeners;

    protected AbstractQualitySessionAwareAction(Lookup context, SessionEventProxy sessionEventProxy, String... properties) {
        this.context = context;

        //The thing we want to listen for the presence or absence of
        //on the global selection
        Lookup.Template<Node> tpl = new Lookup.Template<>(Node.class);
        lkpInfo = context.lookup(tpl);
        lkpInfo.addLookupListener((e) -> updateActionState());

        if (properties.length == 0) {
            weakListeners = new PropertyChangeListener[1];
            weakListeners[0] = sessionEventProxy.addWeakPropertyChangeListener((e) -> updateActionState());
        } else {
            weakListeners = new PropertyChangeListener[properties.length];
            for (int i = 0; i < properties.length; i++) {
                weakListeners[i] = sessionEventProxy.addWeakPropertyChangeListener(properties[i], (e) -> updateActionState());

            }
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

    protected final void updateActionState() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(() -> updateActionState());
            return;
        }
        updateActionStateImpl(getActiveProject());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = getActiveProject();
        if (null != project) {
            if (isEnabledForProject(project)) {
                actionPerformedImpl(e, project);
            }
        }
    }

    protected abstract void updateActionStateImpl(Project project);

    protected abstract boolean isEnabledForProject(Project project);

    protected abstract void actionPerformedImpl(ActionEvent e, Project project);

}
