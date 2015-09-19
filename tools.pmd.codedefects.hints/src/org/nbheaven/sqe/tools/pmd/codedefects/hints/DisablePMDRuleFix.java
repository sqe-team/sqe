/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author fvo
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
