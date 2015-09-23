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
package org.nbheaven.sqe.tools.findbugs.codedefects.projects.freeform.customizer;

import edu.umd.cs.findbugs.config.UserPreferences;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.option.ConfigureDetectorsPanel;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.option.ConfigureFeaturesPanel;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.nbheaven.sqe.core.ui.Constants;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sven
 */
@CompositeCategoryProvider.Registrations({
    @CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-ant-freeform", category=Constants.CUSTOMIZER_CATEGORY_ID),
    @CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-apisupport-project", category=Constants.CUSTOMIZER_CATEGORY_ID),
    @CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", category=Constants.CUSTOMIZER_CATEGORY_ID),
    @CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", category=Constants.CUSTOMIZER_CATEGORY_ID)
})
public class PanelProvider implements CompositeCategoryProvider {

    private static final RequestProcessor RP = new RequestProcessor(PanelProvider.class.getName());

    public Category createCategory(Lookup context) {
        return Category.create("FindBugs", "FindBugs", null);
    }

    public JComponent createComponent(final Category category, Lookup context) {
        Project p = context.lookup(Project.class);
        final FindBugsSettingsProvider fibuSettingsProvider = p.getLookup().lookup(FindBugsSettingsProvider.class);
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Loading..."), BorderLayout.CENTER); // SQE-42
        RP.post(new Runnable() {
            public void run() {
                final UserPreferences findBugsSettings = fibuSettingsProvider.getFindBugsSettings();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        JTabbedPane jTabbedPane = new JTabbedPane();
                        final ConfigureDetectorsPanel detectorsPanel = new ConfigureDetectorsPanel(findBugsSettings);
                        jTabbedPane.addTab("Configure Detectors", detectorsPanel);
                        JPanel featuresPanel = new ConfigureFeaturesPanel();
                        panel.removeAll();
                        jTabbedPane.addTab("Configure Features", featuresPanel);
                        panel.add(jTabbedPane, BorderLayout.CENTER);
                        category.setOkButtonListener(new ActionListener() {
                            public void actionPerformed(ActionEvent actionEvent) {
                                detectorsPanel.applyDetectorChangesToUserPreferences(findBugsSettings);
                                fibuSettingsProvider.setFindBugsSettings(findBugsSettings);
                            }
                        });
                        panel.revalidate();
                    }
                });
            }
        });
        return panel;
    }

}
