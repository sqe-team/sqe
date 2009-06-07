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
package org.nbheaven.sqe.tools.pmd.codedefects.core.settings.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
public final class GlobalPMDSettings implements PMDSettings {

    private static String SETTINGS_DIR = System.getProperty("netbeans.user") + File.separatorChar +
            "config" + File.separatorChar + "pmd";
    private static String SETTINGS_FILE = "pmd.settings";
    private Map<String, Boolean> ruleMap;
    File settingsFile;

    public GlobalPMDSettings() {

        settingsFile = new File(SETTINGS_DIR + File.separatorChar + SETTINGS_FILE);
        readFile();
    }

    public void activateRule(Rule rule) {
        ruleMap.remove(rule.getName());
        updateFile();
    }

    public void deavtivateRule(Rule rule) {
        ruleMap.put(rule.getName(), Boolean.FALSE);
        updateFile();
    }

    public boolean isRuleActive(Rule rule) {
        Boolean b = ruleMap.get(rule.getName());
        return null == b ? true : b.booleanValue();
    }

    public RuleSet getActiveRules() {
        RuleSet activeRuleSet = new RuleSet();
        RuleSet ruleSet = new RuleSet();
        RuleSetFactory ruleSetFactory = new RuleSetFactory();
        try {
            Iterator<RuleSet> iterator = ruleSetFactory.getRegisteredRuleSets();
            while (iterator.hasNext()) {
                ruleSet = iterator.next();
                for (Rule rule : ruleSet.getRules()) {
                    if (isRuleActive(rule)) {
                        activeRuleSet.addRule(rule);
                    }
                }
            }
        } catch (RuleSetNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return activeRuleSet;
    }

    private void updateFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile, false));
            for (Map.Entry<String, Boolean> entry : ruleMap.entrySet()) {
                if (!entry.getValue()) {
                    writer.append(entry.getKey());
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void readFile() {
        ruleMap = new HashMap<String, Boolean>();
        if (settingsFile.exists()) {
            try {
                BufferedReader fileReader = new BufferedReader(new FileReader(settingsFile));
                String line;
                while (null != (line = fileReader.readLine())) {
                    ruleMap.put(line, Boolean.FALSE);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
