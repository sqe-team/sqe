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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.ui.result;

import edu.umd.cs.findbugs.BugAnnotation;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Sven Reimers
 */
class BugAnnotationNode extends DefaultMutableTreeNode {

    private BugAnnotation bugAnnotation;

    /** Creates a new instance of BugInstanceNode */
    BugAnnotationNode(BugAnnotation bugAnnotation) {
        super(bugAnnotation, true);
        this.bugAnnotation = bugAnnotation;
    }

    BugAnnotation getBugAnnotation() {
        return bugAnnotation;
    }
}