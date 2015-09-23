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
package org.nbheaven.sqe.core.ui.nodes;

import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Sven Reimers
 */
/* SQE-28: disable until contains something useful
@NodeFactory.Registration(projectType={
    "org-netbeans-modules-ant-freeform",
    "org-netbeans-modules-autoproject",
    "org-netbeans-modules-apisupport-project",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"
}, position=1000)
 */
public class SQENodeFactory implements NodeFactory {

    public SQENodeFactory() {
    }

    @Override
    public NodeList<?> createNodes(Project project) {
        return NodeFactorySupport.fixedNodeList(new SQENode(Lookups.singleton(project)));
    }

    private static class SQENode extends AbstractNode {

        private final Action[] actions;

        private SQENode(Lookup lookup) {
            super(Children.create(new SQEChildrenFactory(), true), lookup);
            setIconBaseWithExtension("org/nbheaven/sqe/core/ui/resources/sqe_16.png");
            setDisplayName("SQE");
            List<? extends Action> actionsForPath = Utilities.actionsForPath("SQE/Project-Node/Actions");
            actions = actionsForPath.toArray(new Action[actionsForPath.size()]);
        }

        @Override
        public Action getPreferredAction() {
            return actions.length > 0 ? actions[0] : null;
        }

        @Override
        public Action[] getActions(boolean context) {
//            Action[] superActions = super.getActions(context);
//            if (superActions.length > 0) {
//                Action[] allActions = new Action[actions.length + superActions.length];
//                System.arraycopy(actions, 0, allActions, 0, actions.length);
//                System.arraycopy(superActions, 0, allActions, actions.length, superActions.length);
//                return allActions;
//            } else {
                return actions;
//            }
        }
    }

    private static class SQEChildrenFactory extends ChildFactory<Object> {

        @Override
        protected boolean createKeys(List<Object> keyList) {
            return true;
        }
    }
}
