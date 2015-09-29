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
package org.nbheaven.sqe.tools.pmd.codedefects.hints;

import net.sourceforge.pmd.RuleViolation;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDSession;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Florian Vogler
 */
class DisablePMDRuleFix implements Fix {
    private final RuleViolation ruleViolation;
    private final Project project;

    public DisablePMDRuleFix(RuleViolation ruleViolation, Project project) {
        this.ruleViolation = ruleViolation;
        this.project = project;
    }

    @Override
    public String getText() {
        return "Disable PMD Rule: " + ruleViolation.getRule().getName();
    }

    @Override
    public ChangeInfo implement() throws Exception {
        PMDSettingsProvider settingsProvider = project.getLookup().lookup(PMDSettingsProvider.class);
        if (null != settingsProvider) {
            PMDSettings pmdSettings = settingsProvider.getPMDSettings();
            if (pmdSettings != null) {
                pmdSettings.deactivateRule(ruleViolation.getRule());
            }
        }
        PMDSession qualitySession = project.getLookup().lookup(PMDSession.class);
        PMDResult result = qualitySession.getResult();
        if (null != result) {
            result.removeAllRuleViolationsForRule(ruleViolation.getRule());
        }
        return new ChangeInfo();
    }
    
}
