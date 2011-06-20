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
package org.nbheaven.sqe.tools.pmd.codedefects.core.ui.result;

import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.sourceforge.pmd.IRuleViolation;
import net.sourceforge.pmd.Rule;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult.Mode;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDSession;
import org.nbheaven.sqe.tools.pmd.codedefects.core.annotations.RuleViolationAnnotationProcessor;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

class BugTree extends JTree {

    public static final String PROPERTY_ACTIVE_MODE = "ActiveMode";
    public static final String PROPERTY_CORE_FILTER_ENABLED = "CoreFilterEnabled";
    private final RequestProcessor requestProcessor;
    private final PMDSession session;
    private Mode resultMode = Mode.TYPE;
    private boolean isCollapsed = true;
    private boolean coreFilterEnabled = true;

    BugTree(PMDSession session) {
        this.session = session;
        this.requestProcessor = new RequestProcessor("BugTree-" + session.getDisplayName(), 1);
        this.session.addPropertyChangeListener(new SessionListener(this));
        setCellRenderer(RuleViolationRenderer.instance());
        addMouseListener(new JumpToSourceMouseListener());
        addKeyListener(new JumpToSourceKeyListener());
    }

    public PMDSession getSession() {
        return session;
    }

    public boolean isActiveMode(Mode mode) {
        return this.resultMode == mode;
    }

    public void setActiveMode(Mode mode) {
        Mode oldMode = this.resultMode;
        this.resultMode = mode;
        refresh();
        firePropertyChange(PROPERTY_ACTIVE_MODE, oldMode, this.resultMode);
    }

    public boolean isCoreFilterEnabled() {
        return this.coreFilterEnabled;
    }

    public void setCoreFilterEnabled(boolean coreFilterEnabled) {
        boolean oldCoreFilterEnabled = this.coreFilterEnabled;
        this.coreFilterEnabled = coreFilterEnabled;
        refresh();
        firePropertyChange(PROPERTY_CORE_FILTER_ENABLED, oldCoreFilterEnabled, this.coreFilterEnabled);
    }

    public void refresh() {
        requestProcessor.post(new Runnable() {
            public void run() {
                final TreeNode rootNode = createRootTreeNode(session, coreFilterEnabled, resultMode);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        setModel(new DefaultTreeModel(rootNode));
                        if (isCollapsed) {
                            collapseAll();
                        } else {
                            expandAll();
                        }
                    }
                });
            }
        });
    }

    public void collapseAll() {
        int row = getRowCount() - 1;
        while (row > 0) {
            collapseRow(row);
            row--;
        }
    }

    public void expandAll() {
        int row = getRowCount() - 1;
        while (row > 0) {
            expandRow(row);
            row--;
        }
    }

    private static TreeNode createRootTreeNode(PMDSession session, boolean coreFilterEnabled, Mode resultMode) {
        if (null == session || null == session.getResult()) {
            return new DefaultMutableTreeNode("No result available");
        }

        MutableTreeNode rootNode = new SessionNode(session, session.getResult().getBugCount());
        rootNode.setUserObject(session);

        Map<Object, Collection<IRuleViolation>> instances = resultMode.getInstanceList(session.getResult());
        int typeIndex = 0;

        for (Map.Entry<Object, Collection<IRuleViolation>> entry : instances.entrySet()) {
            MutableTreeNode typeNode = new BugGroupNode(entry.getKey(), entry.getValue().size());
            int index = 0;

            for (IRuleViolation ruleViolation : entry.getValue()) {
                RuleViolationNode ruleViolationNode = new RuleViolationNode(ruleViolation, false);
                typeNode.insert(ruleViolationNode, index);
                index++;
            }

            rootNode.insert(typeNode, typeIndex);
            typeIndex++;

        }
        return rootNode;
    }

    @Override
    public JPopupMenu getComponentPopupMenu() {
        TreePath treePath = getSelectionModel().getSelectionPath();
        if (null != treePath && treePath.getPathCount() > 0) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            Object obj = selectedNode.getUserObject();
            if (obj instanceof Rule) {
                Action disableDetector = new DisableRuleAction((Rule) obj, session.getProject());
                return Utilities.actionsToPopup(new Action[]{disableDetector}, Utilities.actionsGlobalContext());
            }
        }
        return null;
    }

    /**
     * Dispatch the event to open the source file according to type of annotation
     */
    private static void jumpToSource(final TreePath treePath) {
        if (null == treePath || treePath.getPathCount() == 0) {
            return;
        }
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    jumpToSource(treePath);
                }
            });
            return;
        }

        PMDSession session = (PMDSession) ((DefaultMutableTreeNode) treePath.getPathComponent(0)).getUserObject();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        Object obj = selectedNode.getUserObject();

        if (obj instanceof IRuleViolation) {
            IRuleViolation ruleViolation = (IRuleViolation) obj;
            RuleViolationAnnotationProcessor.openSourceFile(ruleViolation, session.getProject());
        }
    }

    private static class SessionListener implements PropertyChangeListener {

        private final BugTree bugTreePanel;

        public SessionListener(BugTree bugTreePanel) {
            this.bugTreePanel = bugTreePanel;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            bugTreePanel.refresh();
        }
    }

    private static class JumpToSourceMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent evt) {
            if (2 == evt.getClickCount()) {
                JTree jTree = (JTree) evt.getSource();
                jumpToSource(jTree.getSelectionPath());
            }
        }
    }

    private static class JumpToSourceKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                JTree jTree = (JTree) keyEvent.getSource();
                jumpToSource(jTree.getSelectionPath());
            }
        }

        @Override
        public void keyTyped(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                JTree jTree = (JTree) keyEvent.getSource();
                jumpToSource(jTree.getSelectionPath());
            }
        }
    }
}
