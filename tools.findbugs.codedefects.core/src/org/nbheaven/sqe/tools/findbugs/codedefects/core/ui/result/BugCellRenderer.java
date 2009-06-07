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
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult.CategoryKey;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult.ClassKey;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult.PackageKey;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

/**
 * Custom cell renderer for the bug tree.
 * We use this to select the tree icons, and to set the
 * text color based on the bug priority.
 */
class BugCellRenderer extends DefaultTreeCellRenderer {

    private static final Color HIGH_PRIORITY_COLOR = new Color(0xff0000);
    private static final Color NORMAL_PRIORITY_COLOR = new Color(0x9f0000);
    private static final Color LOW_PRIORITY_COLOR = Color.BLACK;
    private static final Color EXP_PRIORITY_COLOR = Color.BLACK;
    private static final Color IGNORE_PRIORITY_COLOR = Color.LIGHT_GRAY;
    private static final BugCellRenderer theInstance = new BugCellRenderer();
    private static final long serialVersionUID = 1L;
    private final ImageIcon bugGroupIcon;
    private final ImageIcon packageIcon;
    private final ImageIcon bugIcon;
    private final ImageIcon classIcon;
    private final ImageIcon methodIcon;
    private final ImageIcon fieldIcon;
    private final ImageIcon localVariableIcon;
    private final ImageIcon sourceFileIcon;
    private Object value;

    private BugCellRenderer() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        bugGroupIcon = new ImageIcon(classLoader.getResource("edu/umd/cs/findbugs/gui/bug.png"));
        packageIcon = new ImageIcon(classLoader.getResource("org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/package.gif"));
        bugIcon = new ImageIcon(classLoader.getResource("edu/umd/cs/findbugs/gui/bug2.png"));
        classIcon = new ImageIcon(classLoader.getResource("org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/class.gif"));
        methodIcon = new ImageIcon(classLoader.getResource("edu/umd/cs/findbugs/gui/method.png"));
        fieldIcon = new ImageIcon(classLoader.getResource("edu/umd/cs/findbugs/gui/field.png"));
        localVariableIcon = new ImageIcon(classLoader.getResource("edu/umd/cs/findbugs/gui/field.png")); //TODO find icon
        sourceFileIcon = new ImageIcon(classLoader.getResource("edu/umd/cs/findbugs/gui/sourcefile.png"));
    }

    /**
     * Get the single instance.
     *
     * @return the instance
     */
    public static BugCellRenderer instance() {
        return theInstance;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        // Set the icon, depending on what kind of node it is
        if (value instanceof BugAnnotationNode) {
            BugAnnotation bugAnnotation = ((BugAnnotationNode) value).getBugAnnotation();
            if (bugAnnotation instanceof ClassAnnotation) {
                setIcon(classIcon);
            } else if (bugAnnotation instanceof MethodAnnotation) {
                setIcon(methodIcon);
            } else if (bugAnnotation instanceof FieldAnnotation) {
                setIcon(fieldIcon);
            } else if (bugAnnotation instanceof SourceLineAnnotation) {
                setIcon(sourceFileIcon);
            } else {
                setIcon(null);
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new NotImplementedException("Missing Icon for BugAnnotation: " + bugAnnotation.getClass())); //NOI18N
            }

        } else if (value instanceof BugInstanceNode) {
            BugInstance bugInstance = ((BugInstanceNode) value).getBugInstance();
            setIcon(bugIcon);
            setText(bugInstance.getMessage());

        } else if (value instanceof SessionNode) {
            SessionNode sessionNode = (SessionNode) value;
            ProjectInformation information = ProjectUtils.getInformation(sessionNode.getSession().getProject());
            setIcon(information.getIcon());
            setText(information.getDisplayName() + " (" + sessionNode.getBugCount() + ")");

        } else if (value instanceof BugGroupNode) {
            BugGroupNode bugGroupNode = (BugGroupNode) value;

            Object groupObject = bugGroupNode.getGroupObject();
            if (groupObject instanceof FindBugsResult.ClassKey) {
                setIcon(classIcon);
                setText(((ClassKey) groupObject).getDisplayName());
            } else if (groupObject instanceof FindBugsResult.PackageKey) {
                setIcon(packageIcon);
                setText(((PackageKey) groupObject).getDisplayName());
            } else if (groupObject instanceof FindBugsResult.CategoryKey) {
                setIcon(bugGroupIcon);
                setText(((CategoryKey) groupObject).getDisplayName());
            } else if (groupObject instanceof BugPattern) {
                BugPattern bugPattern = (BugPattern) groupObject;
                setIcon(bugGroupIcon);
                setText(bugPattern.getAbbrev() + ": " + bugPattern.getShortDescription());
                setToolTipText(bugPattern.getDetailHTML());
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

        if (value instanceof BugInstance) {
            BugInstance bugInstance = (BugInstance) value;

            switch (bugInstance.getPriority()) {
                case Detector.HIGH_PRIORITY:
                    color = HIGH_PRIORITY_COLOR;
                    break;

                case Detector.NORMAL_PRIORITY:
                    color = NORMAL_PRIORITY_COLOR;
                    break;

                case Detector.LOW_PRIORITY:
                    color = LOW_PRIORITY_COLOR;
                    break;

                case Detector.EXP_PRIORITY:
                    color = EXP_PRIORITY_COLOR;
                    break;

                case Detector.IGNORE_PRIORITY:
                    color = IGNORE_PRIORITY_COLOR;
                    break;
            }
        }

        return color;
    }
}
