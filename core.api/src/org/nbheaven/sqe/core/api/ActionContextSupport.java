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
package org.nbheaven.sqe.core.api;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 *
 * @author Florian Vogler
 */
public final class ActionContextSupport {

    private ActionContextSupport() {
    }

    public static Project getActiveProject() {
        return findProject(Utilities.actionsGlobalContext().lookupAll(Node.class));

    }

    public static Project findProject(Node[] nodes) {
        return findProject(Arrays.asList(nodes));
    }

    public static Project findProject(Collection<? extends Node> nodes) {
        if ((nodes == null) || (nodes.size() == 0)) {
            return null;
        }
        Node node = nodes.iterator().next();
        DataObject dao = node.getCookie(DataObject.class);

        if (dao != null) {
            return FileOwnerQuery.getOwner(dao.getPrimaryFile());
        } else {
            Project p = node.getLookup().lookup(Project.class);
            return p;
        }
    }
}
