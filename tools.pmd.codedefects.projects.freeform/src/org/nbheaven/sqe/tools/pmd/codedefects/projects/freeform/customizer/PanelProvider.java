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
import org.nbheaven.sqe.core.ui.Constants;
import org.nbheaven.sqe.tools.pmd.codedefects.core.option.ConfigureRulesPanel;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Lookup;

/**
 *
 * @author antoni
 */
@CompositeCategoryProvider.Registrations({
    @CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-ant-freeform", category=Constants.CUSTOMIZER_CATEGORY_ID),
    @CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-apisupport-project", category=Constants.CUSTOMIZER_CATEGORY_ID),
    @CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", category=Constants.CUSTOMIZER_CATEGORY_ID),
    @CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-web-project", category=Constants.CUSTOMIZER_CATEGORY_ID)
})
public class PanelProvider implements CompositeCategoryProvider {

    @Override
    public Category createCategory(Lookup lookup) {
        return Category.create("PMD", "PMD", null);
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        Project p = context.lookup(Project.class);

        final PMDSettingsProvider pmdSettingsProvider = p.getLookup().lookup(PMDSettingsProvider.class);

        final PMDSettings pmdSettings = pmdSettingsProvider.getPMDSettings();

        final ConfigureRulesPanel rulesPanel = new ConfigureRulesPanel();
        rulesPanel.setSettings(pmdSettings);

        category.setOkButtonListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                rulesPanel.saveSettingsToPreferences();
            }
        });
        panel.add(rulesPanel, BorderLayout.CENTER);


        return panel;
    }

}
