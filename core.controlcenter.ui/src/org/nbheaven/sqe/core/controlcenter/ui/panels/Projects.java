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
package org.nbheaven.sqe.core.controlcenter.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.nbheaven.sqe.core.api.SQEManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sven
 */
public class Projects extends JPanel {

    private JComboBox<Project> openProjectsComboBox;
    private DefaultComboBoxModel<Project> projectModel;
    private final SelectedProjectListener selectedProjectListener;
    private final SQEManagerListener sqeManagerListener;
    private final OpenProjectsListner openProjectsListner;

    public Projects() {
        initComponents();

        sqeManagerListener = new SQEManagerListener(this);
        openProjectsListner = new OpenProjectsListner(this);

        selectedProjectListener = new SelectedProjectListener(openProjectsComboBox);
        openProjectsComboBox.addItemListener(selectedProjectListener);
        openProjectsComboBox.addActionListener(selectedProjectListener);
    }

    private void initComponents() {
        projectModel = new DefaultComboBoxModel<Project>(getOpenedProjectsSorted().toArray(new Project[0]));
        openProjectsComboBox = new JComboBox<Project>(projectModel);
        openProjectsComboBox.setRenderer(new OpenProjectsListCellRenderer());

        this.setLayout(new BorderLayout());
        this.add(openProjectsComboBox, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        this.setBackground(Color.WHITE);
        this.setOpaque(true);
        Dimension preferredSize = openProjectsComboBox.getPreferredSize();
        preferredSize.width = 0;
        openProjectsComboBox.setPreferredSize(preferredSize);

    }

    @Override
    public void addNotify() {
        super.addNotify();
        OpenProjects.getDefault().addPropertyChangeListener(openProjectsListner);
        SQEManager.getDefault().addPropertyChangeListener(sqeManagerListener);
        updateOpenProjects();
    }

    @Override
    public void removeNotify() {
        OpenProjects.getDefault().removePropertyChangeListener(openProjectsListner);
        SQEManager.getDefault().removePropertyChangeListener(sqeManagerListener);
        super.removeNotify();
    }

    private void updateSelectedProject() {
        Object o = SQEManager.getDefault().getActiveProject();
        if (projectModel.getSize() == 0) {
            openProjectsComboBox.setEnabled(false);
            openProjectsComboBox.setSelectedIndex(-1);
        } else if (-1 != projectModel.getIndexOf(o)) {
            openProjectsComboBox.setEnabled(true);
            openProjectsComboBox.setSelectedItem(o);
        } else {
            openProjectsComboBox.setEnabled(true);
            openProjectsComboBox.setSelectedIndex(0);
        }
    }

    private void updateOpenProjects() {
        openProjectsComboBox.removeItemListener(selectedProjectListener);
        openProjectsComboBox.removeActionListener(selectedProjectListener);
        projectModel.removeAllElements();
        for (Project project : getOpenedProjectsSorted()) {
            projectModel.addElement(project);
        }
        openProjectsComboBox.addItemListener(selectedProjectListener);
        openProjectsComboBox.addActionListener(selectedProjectListener);
        updateSelectedProject();
    }

    public static Set<Project> getOpenedProjectsSorted() {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        Set<Project> sorted = new TreeSet<Project>(new Comparator<Project>() {

            @Override
            public int compare(Project o1, Project o2) {
                String s1 = ProjectUtils.getInformation(o1).getDisplayName();
                String s2 = ProjectUtils.getInformation(o2).getDisplayName();
                return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
            }
        });
        sorted.addAll(Arrays.asList(openProjects));
        return sorted;
    }

    private static class SelectedProjectListener implements ItemListener, ActionListener {

        private final JComboBox<Project> comboBox;

        public SelectedProjectListener(JComboBox<Project> comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (ItemEvent.SELECTED == e.getStateChange()) {
                Project project = (Project) e.getItem();
                SQEManager.getDefault().setActiveProject(project);
                this.comboBox.setToolTipText(FileUtil.getFileDisplayName(project.getProjectDirectory()));
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Project project = (Project) comboBox.getSelectedItem();
            SQEManager.getDefault().setActiveProject(project);
        }
    }

    private static class SQEManagerListener implements PropertyChangeListener {

        private final Projects projects;

        public SQEManagerListener(Projects projects) {
            this.projects = projects;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            projects.updateSelectedProject();
        }
    }

    private static class OpenProjectsListner implements PropertyChangeListener {

        private final Projects projects;

        public OpenProjectsListner(Projects projects) {
            this.projects = projects;
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        propertyChange(evt);
                    }
                });
                return;
            }
            projects.updateOpenProjects();
        }
    }

    private static class OpenProjectsListCellRenderer extends DefaultListCellRenderer {

        public OpenProjectsListCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel && null != value) {
                Project project = (Project) value;
                JLabel label = (JLabel) c;
                label.setText(ProjectUtils.getInformation(project).getDisplayName());
                label.setIcon(ProjectUtils.getInformation(project).getIcon());
                label.setToolTipText(FileUtil.getFileDisplayName(project.getProjectDirectory()));
                if (-1 < index) {
                    list.setToolTipText(FileUtil.getFileDisplayName(project.getProjectDirectory()));
                }
            }
            return c;
        }
    }
}
