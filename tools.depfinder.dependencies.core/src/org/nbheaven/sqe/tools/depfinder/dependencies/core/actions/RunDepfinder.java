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
package org.nbheaven.sqe.tools.depfinder.dependencies.core.actions;

import org.nbheaven.sqe.tools.depfinder.dependencies.core.resources.ResourcesConsts;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.DependencyViewTopComponent;
import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

/**
 * Action to trigger findbugs run on actual project
 * @author reimers-sven
 */
class RunDepfinder extends NodeAction {

    public RunDepfinder() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        setEnabled(false);
    }

    public String getName() {
        return NbBundle.getMessage(RunDepfinder.class, "LBL_Action");
    }

    @Override
    protected String iconResource() {
        return ResourcesConsts.DEPFINDER_ICON_PATH;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(Node[] nodes) {
        if (enable(nodes)) {
            Project project = SQEProjectSupport.findProject(nodes[0]);
            TopComponent tc = new DependencyViewTopComponent(project, nodes);
            tc.open();
            tc.requestActive();
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (ProjectUtilities.areJavaSourcePackages(nodes)) {
            return true;
        }
        if (nodes.length != 1) {
            return false;
        }
        Project project = SQEProjectSupport.findProject(nodes[0]);
        if (project == null) {
            return false;
        }
        SourceGroup[] sg = ProjectUtilities.getJavaSourceGroups(project);
        return sg.length > 0;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
