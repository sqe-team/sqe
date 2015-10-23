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
package org.nbheaven.sqe.tools.pmd.codedefects.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult.CategoryKey;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult.ClassKey;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult.PackageKey;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Sven Reimers
 */
public final class PMDResultImpl implements PMDResult {

    private Map<ClassKey, Collection<RuleViolation>> instanceByClass;
    private Map<PackageKey, Collection<RuleViolation>> instanceByPackage;
    private Map<CategoryKey, Collection<RuleViolation>> instanceByType;
    private Report report;
    private Lookup lookup;

    /**
     * Creates a new instance of PMDResult
     */
    PMDResultImpl(Report report) {
        this.report = report;
        this.lookup = Lookups.singleton(this);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private void removeAllRuleViolationsForRule(Rule rule, Map<? extends Object, Collection<RuleViolation>> mapToClear) {
        for (Map.Entry<Object, Collection<RuleViolation>> entry : new HashMap<>(mapToClear).entrySet()) {
            for (RuleViolation ruleViolation : new ArrayList<>(entry.getValue())) {
                if (ruleViolation.getRule().equals(rule)) {
                    entry.getValue().remove(ruleViolation);
                }
            }
            if (entry.getValue().isEmpty()) {
                mapToClear.remove(entry.getKey());
            }
        }
    }

    @Override
    public synchronized void removeAllRuleViolationsForRule(Rule rule) {
        if (null != instanceByType) {
            removeAllRuleViolationsForRule(rule, instanceByType);
        }
        if (null != instanceByClass) {
            removeAllRuleViolationsForRule(rule, instanceByClass);
        }
        if (null != instanceByPackage) {
            removeAllRuleViolationsForRule(rule, instanceByPackage);
        }
//        session.resultChanged(null, this);
    }

    @Override
    public synchronized Map<CategoryKey, Collection<RuleViolation>> getInstanceByType() {
        if (null == instanceByType) {
            Map<CategoryKey, Collection<RuleViolation>> tempInstanceByType = new TreeMap<>();

            Iterator<RuleViolation> ruleViolationIterator = report.iterator();

            while (ruleViolationIterator.hasNext()) {
                RuleViolation ruleViolation = ruleViolationIterator.next();
                CategoryKey categoryKey = new CategoryKey(ruleViolation);
                Collection<RuleViolation> ruleViolations = tempInstanceByType.get(categoryKey);

                if (null == ruleViolations) {
                    ruleViolations = new ArrayList<>();
                    tempInstanceByType.put(categoryKey, ruleViolations);
                }

                ruleViolations.add(ruleViolation);
            }
            instanceByType = tempInstanceByType;
        }

        return instanceByType;
    }

    @Override
    public synchronized Map<ClassKey, Collection<RuleViolation>> getInstanceByClass() {

        if (null == instanceByClass) {
            instanceByClass = new TreeMap<>();

            Iterator<RuleViolation> ruleViolationIterator = report.iterator();

            while (ruleViolationIterator.hasNext()) {
                RuleViolation ruleViolation = ruleViolationIterator.next();
                ClassKey classKey = new ClassKey(ruleViolation);
                Collection<RuleViolation> ruleViolations = instanceByClass.get(classKey);

                if (null == ruleViolations) {
                    ruleViolations = new ArrayList<>();
                    instanceByClass.put(classKey, ruleViolations);
                }

                ruleViolations.add(ruleViolation);
            }
        }

        return instanceByClass;
    }

    @Override
    public synchronized Map<PackageKey, Collection<RuleViolation>> getInstanceByPackage() {
        if (null == instanceByPackage) {
            instanceByPackage = new TreeMap<>();

            Iterator<RuleViolation> ruleViolationIterator = report.iterator();

            while (ruleViolationIterator.hasNext()) {
                RuleViolation ruleViolation = ruleViolationIterator.next();
                PackageKey packageKey = new PackageKey(ruleViolation);
                Collection<RuleViolation> ruleViolations = instanceByPackage.get(packageKey);

                if (null == ruleViolations) {
                    ruleViolations = new ArrayList<>();
                    instanceByPackage.put(packageKey, ruleViolations);
                }

                ruleViolations.add(ruleViolation);
            }
        }

        return instanceByPackage;
    }

    public long getBugCount() {
        return report.size();
    }

    @Override
    public long getCodeDefectCountSum() {
        return getBugCount();
    }

    @Override
    public long getCodeDefectCount(CodeDefectSeverity severity) {
        if (CodeDefectSeverity.INFO == severity) {
            return getBugCount();
        }
        if (CodeDefectSeverity.WARNING == severity) {
            return 0;
        }
        if (CodeDefectSeverity.ERROR == severity) {
            return 0;
        }

        return 0;
    }

}
