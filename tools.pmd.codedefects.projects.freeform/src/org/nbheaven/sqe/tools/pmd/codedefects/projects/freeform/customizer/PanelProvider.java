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
package org.nbheaven.sqe.tools.pmd.codedefects.projects.freeform.customizer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.nbheaven.sqe.tools.pmd.codedefects.core.option.ConfigureRulesPanel;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;

/**
 *
 * @author antoni
 */
public class PanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private final String name;

    /** Creates a new instance of ProjectPanelProvider */
    private PanelProvider(String name) {
        this.name = name;
    }

    public Category createCategory(Lookup lookup) {
        return ProjectCustomizer.Category.create(this.name, "PMD", null);
    }

    public JComponent createComponent(Category category, Lookup context) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        Project p = context.lookup(Project.class);

        final PMDSettingsProvider pmdSettingsProvider = p.getLookup().lookup(PMDSettingsProvider.class);

        final PMDSettings pmdSettings = pmdSettingsProvider.getPMDSettings();

        final ConfigureRulesPanel rulesPanel = new ConfigureRulesPanel(pmdSettings);

        category.setOkButtonListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                rulesPanel.saveSettingsToPreferences();
            }
        });
        panel.add(rulesPanel, BorderLayout.CENTER);


        return panel;
    }

    public static PanelProvider createExample() {
        return new PanelProvider("Example");
    }
}
