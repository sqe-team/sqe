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
package org.nbheaven.sqe.informations.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import org.nbheaven.sqe.informations.ui.spi.SQEInformationProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Sven Reimers
 */
public class InformationTopComponent extends TopComponent implements PropertyChangeListener {

    private static Map<Project, TopComponent> instances = new WeakHashMap<Project, TopComponent>();
    private final Project project;

    private InformationTopComponent(Project project) {
        this.associateLookup(Lookups.fixed(new AbstractNode(Children.LEAF, Lookups.fixed(project))));
        this.project = project;
        initComponent();
        OpenProjects.getDefault().addPropertyChangeListener(this);
    }

    public static synchronized TopComponent getInstance(Project project) {
        assert EventQueue.isDispatchThread() : "Call this from EventQueue";
        TopComponent tc = instances.get(project);
        if (null == tc) {
            tc = new InformationTopComponent(project);
            instances.put(project, tc);
        }
        return tc;
    }

    private void initComponent() {
        this.setBackground(Color.WHITE);
        this.setOpaque(true);
        this.setLayout(new BorderLayout());
        ProjectInformation information = ProjectUtils.getInformation(project);
        JLabel label = new JLabel(information.getDisplayName() + " [" + information.getName() + "]", information.getIcon(), JLabel.LEADING);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(label, BorderLayout.NORTH);
        SQEInformationProvider provider = project.getLookup().lookup(SQEInformationProvider.class);
        this.add(provider.getInformationComponent().getComponent(), BorderLayout.CENTER);
    }

    @Override
    protected void componentClosed() {
        OpenProjects.getDefault().removePropertyChangeListener(this);
        instances.remove(project);
    }

    @Override
    public String getDisplayName() {
        return "SQE Information [" + ProjectUtils.getInformation(project).getDisplayName() + "]";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/nbheaven/sqe/informations/ui/resources/info.png");
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected String preferredID() {
        return getClass().getName() + project.hashCode();
    }

    public void propertyChange(PropertyChangeEvent event) {
        // The list of open projects has changed
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(event.getPropertyName())) {
            List<Project> projects = Arrays.asList(OpenProjects.getDefault().getOpenProjects());
            if (!projects.contains(project)) {
                this.close();
            }
        }
    }
}
