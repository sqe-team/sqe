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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.ui.result;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import javax.swing.tree.DefaultMutableTreeNode;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleSession;

/**
 *
 * @author sven
 */
class AuditEventNode extends DefaultMutableTreeNode {

    private final AuditEvent auditEvent;
    private CheckstyleSession session;

    /**
     * Creates a new instance of AuditEventNode
     */
    AuditEventNode(AuditEvent auditEvent, CheckstyleSession session) {
        super(auditEvent, false);
        this.auditEvent = auditEvent;
        this.session = session;
    }

    AuditEvent getAuditEvent() {
        return auditEvent;
    }

    String getRelativeFileName() {
        String path = session.getProject().getProjectDirectory().getPath();
        String fileName = auditEvent.getFileName();
        fileName = fileName.replaceAll("\\\\", "/");
        if (fileName.startsWith(path)) {
            fileName = fileName.substring(path.length() + 1);
        }
        return fileName;
    }
}
