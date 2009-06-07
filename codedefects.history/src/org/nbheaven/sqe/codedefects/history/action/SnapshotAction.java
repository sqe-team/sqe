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

import javax.swing.Action;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.history.util.CodeDefectHistoryPersistence;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Sven Reimers
 */
public class SnapshotAction extends NodeAction {
    public SnapshotAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(Action.SHORT_DESCRIPTION,
            NbBundle.getMessage(SnapshotAction.class, "HINT_Action"));
        setEnabled(false);
    }

    public String getName() {
        return NbBundle.getMessage(SnapshotAction.class, "LBL_Action");
    }

    @Override
    protected String iconResource() {
        return "org/nbheaven/sqe/codedefects/history/resources/camera.png";
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(Node[] nodes) {
        if (enable(nodes)) {
            Project project = SQEProjectSupport.findProject(nodes[0]);
            CodeDefectHistoryPersistence.addSnapshot(project);
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (null == nodes || nodes.length > 1 || 0 == nodes.length) {
            return false;
        }
        Project project = SQEProjectSupport.findProject(nodes[0]);
        
        return (null == project || null == project.getLookup().lookup(QualitySession.class));
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
