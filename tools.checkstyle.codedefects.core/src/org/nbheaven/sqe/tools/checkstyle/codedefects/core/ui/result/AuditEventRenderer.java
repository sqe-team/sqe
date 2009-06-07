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

/**
 *
 * @author sven
 */
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult.CategoryKey;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult.ClassKey;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult.PackageKey;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

/**
 * Custom cell renderer for the bug tree.
 * We use this to select the tree icons, and to set the
 * text color based on the bug priority.
 */
class AuditEventRenderer extends DefaultTreeCellRenderer {

    private static final Color HIGH_PRIORITY_COLOR = new Color(0xff0000);
    private static final Color NORMAL_PRIORITY_COLOR = new Color(0x9f0000);
    private static final Color LOW_PRIORITY_COLOR = Color.BLACK;
    private static final Color IGNORE_PRIORITY_COLOR = Color.LIGHT_GRAY;
    private static final AuditEventRenderer theInstance = new AuditEventRenderer();
    private static final long serialVersionUID = 1L;
    private ImageIcon bugGroupIcon;
    private ImageIcon packageIcon;
    private ImageIcon bugIcon;
    private ImageIcon classIcon;
    //	private ImageIcon methodIcon;
    //	private ImageIcon fieldIcon;
    private ImageIcon sourceFileIcon;
    private Object value;

    private AuditEventRenderer() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        bugGroupIcon = new ImageIcon(classLoader.getResource("org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/checkstyle.png"));
        packageIcon = new ImageIcon(classLoader.getResource("org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/package.gif"));
        bugIcon = new ImageIcon(classLoader.getResource("org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/checkstyle.png"));

        classIcon = new ImageIcon(classLoader.getResource("org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/class.gif"));
        //		methodIcon = new ImageIcon(classLoader.getResource("edu/umd/cs/findbugs/gui/method.png"));
        //		fieldIcon = new ImageIcon(classLoader.getResource("edu/umd/cs/findbugs/gui/field.png"));
        sourceFileIcon = new ImageIcon(classLoader.getResource("org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/sourcefile.png"));
    }

    /**
     * Get the single instance.
     *
     * @return the instance
     */
    public static AuditEventRenderer instance() {
        return theInstance;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        // Set the icon, depending on what kind of node it is
        if (value instanceof AuditEventNode) {
            setIcon(bugIcon);
            AuditEventNode node = (AuditEventNode) value;
            AuditEvent auditEvent = node.getAuditEvent();
            String classDescription = node.getRelativeFileName();
            setText(classDescription + " [Line: " + auditEvent.getLine() + " Column: " + auditEvent.getColumn() + "]");
            setIcon(sourceFileIcon);

        } else if (value instanceof SessionNode) {
            SessionNode sessionNode = (SessionNode) value;
            ProjectInformation information = ProjectUtils.getInformation(sessionNode.getSession().getProject());
            setIcon(information.getIcon());
            setText(information.getDisplayName() + " (" + sessionNode.getBugCount() + ")");

        } else if (value instanceof BugGroupNode) {
            BugGroupNode bugGroupNode = (BugGroupNode) value;

            Object groupObject = bugGroupNode.getGroupObject();
            if (groupObject instanceof CheckstyleResult.ClassKey) {
                setIcon(classIcon);
                setText(((ClassKey) groupObject).getDisplayName());
            } else if (groupObject instanceof CheckstyleResult.PackageKey) {
                setIcon(packageIcon);
                setText(((PackageKey) groupObject).getDisplayName());
            } else if (groupObject instanceof CheckstyleResult.CategoryKey) {
                setIcon(bugGroupIcon);
                setText(((CategoryKey) groupObject).getDisplayName());
            } else {
                setIcon(null);
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new NotImplementedException("Missing Icon for GroupObject: " + groupObject.getClass())); //NOI18N
            }
            setText(getText() + " (" + bugGroupNode.getSize() + ")");
        } else {
            setIcon(null);
        }
        return this;
    }

    @Override
    public Color getTextNonSelectionColor() {
        return getCellTextColor();
    }

    private Color getCellTextColor() {
        // Based on the priority, color-code the bug instance.
        Color color = Color.BLACK;

        if (value instanceof AuditEvent) {
            AuditEvent auditEvent = (AuditEvent) value;

            switch (auditEvent.getSeverityLevel()) {
                case ERROR:
                    color = HIGH_PRIORITY_COLOR;
                    break;

                case WARNING:
                    color = NORMAL_PRIORITY_COLOR;
                    break;

                case INFO:
                    color = LOW_PRIORITY_COLOR;
                    break;

                case IGNORE:
                    color = IGNORE_PRIORITY_COLOR;
                    break;
            }
        }

        return color;
    }
}
