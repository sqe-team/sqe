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
package org.nbheaven.sqe.informations.ui.actions;

import org.nbheaven.sqe.core.utilities.SQEProjectSupport;

import org.netbeans.api.project.Project;

import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

import org.openide.windows.TopComponent;

import javax.swing.Action;
import org.nbheaven.sqe.informations.ui.spi.SQEInformationProvider;
import org.nbheaven.sqe.informations.ui.view.InformationTopComponent;

/**
 *
 * @author Sven Reimers
 */
public class SQEInformationAction extends NodeAction {

    public SQEInformationAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(SQEInformationAction.class, "HINT_Action"));
        setEnabled(false);
    }

    public String getName() {
        return NbBundle.getMessage(SQEInformationAction.class, "LBL_Action");
    }

    @Override
    protected String iconResource() {
        return "org/nbheaven/sqe/informations/ui/resources/info.png";
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(Node[] nodes) {
        if (enable(nodes)) {
            Project project = SQEProjectSupport.findProject(nodes[0]);
            TopComponent informationTopComponent = InformationTopComponent.getInstance(project);
            informationTopComponent.open();
            informationTopComponent.requestActive();
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (null == nodes || nodes.length > 1 || 0 == nodes.length) {
            return false;
        }
        Project project = SQEProjectSupport.findProject(nodes[0]);
        if (null == project || null == project.getLookup().lookup(SQEInformationProvider.class)) {
            return false;
        }
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
