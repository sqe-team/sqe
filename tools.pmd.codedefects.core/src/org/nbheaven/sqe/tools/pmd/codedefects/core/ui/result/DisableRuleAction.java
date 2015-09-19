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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import net.sourceforge.pmd.Rule;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDSession;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.netbeans.api.project.Project;

/**
 *
 * @author Sven Reimers
 */
class DisableRuleAction extends AbstractAction {

    private final Rule rule;
    private final Project project;

    DisableRuleAction(Rule rule, Project project) {
        this.rule = rule;
        this.project = project;
        putValue(Action.NAME, "Disable rule");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PMDSettingsProvider settingsProvider = project.getLookup().lookup(PMDSettingsProvider.class);
        if (null != settingsProvider) {
            PMDSettings pmdSettings = settingsProvider.getPMDSettings();
            pmdSettings.deactivateRule(rule);
        }

        PMDSession qualitySession = project.getLookup().lookup(PMDSession.class);
        PMDResult result = qualitySession.getResult();
        result.removeAllRuleViolationsForRule(rule);
    }
}
