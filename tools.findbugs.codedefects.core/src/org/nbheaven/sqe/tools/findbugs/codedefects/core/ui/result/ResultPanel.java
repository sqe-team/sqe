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

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import java.awt.BorderLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult.Mode;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;

/**
 *
 * @author Sven Reimers
 */
public final class ResultPanel extends JPanel {

    private BugTree bugTree = null;
    private JScrollPane treeScrollPane = null;
    private JScrollPane htmlScrollPane = null;
    private JSplitPane splitPane;
    private JTextPane textPane;
    private ButtonGroup buttonGroup;
    private int dividerLocation = 500;
    private JToggleButton showHideDescriptionButton;

    public ResultPanel(FindBugsSession findBugsSession) {
        initialize(findBugsSession);
        bugTree.refresh();
    }

    private void initialize(final FindBugsSession findBugsSession) {
        setFocusCycleRoot(true);
        setLayout(new BorderLayout());

        bugTree = new BugTree(findBugsSession);

        treeScrollPane = new JScrollPane(bugTree);
        treeScrollPane.setBorder(null);

        textPane = new JTextPane();
        textPane.setSize(200, 100);
        textPane.setEditorKit(new HTMLEditorKit());

        htmlScrollPane = new JScrollPane(textPane);
        htmlScrollPane.setBorder(null);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, htmlScrollPane);
        splitPane.setBorder(null);

        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.add(new RefreshAction(bugTree));
        toolBar.add(new CoreFilterToggleAction(bugTree));
        toolBar.addSeparator();
        toolBar.add(new ExpandAllAction(bugTree));
        toolBar.add(new CollapseAllAction(bugTree));
        toolBar.addSeparator();

        showHideDescriptionButton = new JToggleButton(new ShowHideDescriptionAction(this));
        showHideDescriptionButton.setText(null);
        showHideDescriptionButton.setSelected(true);
        toolBar.add(showHideDescriptionButton);
        toolBar.addSeparator();

        buttonGroup = new ButtonGroup();
        for (Mode mode : Mode.values()) {
            JToggleButton button = new JToggleButton(new SortAction(bugTree, mode));
            buttonGroup.add(button);
            button.setFocusable(false);
            button.setText(null);
            if (bugTree.isActiveMode(mode)) {
                button.setSelected(true);
            }
            toolBar.add(button);
        }

        this.add(toolBar, BorderLayout.WEST);
        this.add(splitPane, BorderLayout.CENTER);
        validate();

        splitPane.setDividerLocation(dividerLocation);
        updateDescription();

        bugTree.addTreeSelectionListener(new DescriptionUpdateListener(this));
    }

    private void setDescriptionVisible(boolean visible) {
        if (visible ^ htmlScrollPane.isVisible()) {
            if (visible) {
                splitPane.setDividerLocation(dividerLocation);
                htmlScrollPane.setVisible(true);
            } else {
                if (htmlScrollPane.isVisible()) {
                    dividerLocation = splitPane.getDividerLocation();
                }
                splitPane.setDividerLocation(1.0);
                htmlScrollPane.setVisible(false);
            }
        }
    }

    void updateDescription() {
        String description = null;

        TreePath treePath = bugTree.getSelectionPath();
        if (null != treePath) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            Object obj = selectedNode.getUserObject();
            if (obj instanceof BugPattern) {
                BugPattern bugPattern = (BugPattern) obj;
                description = bugPattern.getDetailHTML();
            } else {
                BugInstance bugInstance = null;
                if (obj instanceof SourceLineAnnotation) {
                    bugInstance = (BugInstance) ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject();
                } else if (obj instanceof ClassAnnotation) {
                    bugInstance = (BugInstance) ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject();
                } else if (obj instanceof MethodAnnotation) {
                    bugInstance = (BugInstance) ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject();
                } else if (obj instanceof FieldAnnotation) {
                    bugInstance = (BugInstance) ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject();
                } else if (obj instanceof BugInstance) {
                    bugInstance = (BugInstance) obj;
                }
                if (null != bugInstance) {
                    description = bugInstance.getBugPattern().getDetailHTML();
                }
            }
        }

        description = null == description ? "" : description.trim();
        if (description.length() == 0 || !showHideDescriptionButton.isSelected()) {
            textPane.setText("");
            setDescriptionVisible(false);

        } else {
            setDescriptionVisible(true);
            textPane.setText(description);
        }
    }

    private static class DescriptionUpdateListener implements TreeSelectionListener {

        private final ResultPanel resultPanel;

        private DescriptionUpdateListener(ResultPanel resultPanel) {
            this.resultPanel = resultPanel;
        }

        public void valueChanged(TreeSelectionEvent e) {
            resultPanel.updateDescription();
        }
    }
}
