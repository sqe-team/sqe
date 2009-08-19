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
package org.nbheaven.sqe.tools.pmd.codedefects.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.sourceforge.pmd.IRuleViolation;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.tools.pmd.codedefects.core.annotations.RuleViolationAnnotationProcessor;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sven
 */
public class PMDResult implements QualityResult, Lookup.Provider, QualityResultStatistic {

    public enum Mode {

        CLASS("HINT_VIEW_BY_CLASS", "org/nbheaven/sqe/tools/pmd/codedefects/core/resources/class.gif") {

            public Map<Object, Collection<IRuleViolation>> getInstanceList(final PMDResult result) {
                return result.getInstanceByClass();
            }
        },
        PACKAGE("HINT_VIEW_BY_PACKAGE", "org/nbheaven/sqe/tools/pmd/codedefects/core/resources/package.gif") {

            public Map<Object, Collection<IRuleViolation>> getInstanceList(final PMDResult result) {
                return result.getInstanceByPackage();
            }
        },
        TYPE("HINT_VIEW_BY_CATEGORY", "org/nbheaven/sqe/tools/pmd/codedefects/core/resources/pmd.png") {

            public Map<Object, Collection<IRuleViolation>> getInstanceList(final PMDResult result) {
                return result.getInstanceByType();
            }
        };
        private final String hint;
        private final Icon icon;

        Mode(String hint, String iconPath) {
            this.hint = hint;
            icon = new ImageIcon(ImageUtilities.loadImage(iconPath));
        }

        public String getHint() {
            return hint;
        }

        public Icon getIcon() {
            return icon;
        }

        public abstract Map<Object, Collection<IRuleViolation>> getInstanceList(final PMDResult result);
    }
    private Map<Object, Collection<IRuleViolation>> instanceByClass;
    private Map<Object, Collection<IRuleViolation>> instanceByPackage;
    private Map<Object, Collection<IRuleViolation>> instanceByType;
    private Report report;
//    private PMDSession session;
    private Lookup lookup;

    /**
     * Creates a new instance of PMDResult
     */
    public PMDResult(Report report /*, PMDSession session*/) {
        this.report = report;
//        this.session = session;
        lookup = Lookups.fixed(new Object[]{this});
    }

    public Lookup getLookup() {
        return lookup;
    }

    private void removeAllRuleViolationsForRule(Rule rule, Map<? extends Object, Collection<IRuleViolation>> mapToClear) {
        for (Map.Entry<Object, Collection<IRuleViolation>> entry : new HashMap<Object, Collection<IRuleViolation>>(mapToClear).entrySet()) {
            for (IRuleViolation ruleViolation : new ArrayList<IRuleViolation>(entry.getValue())) {
                if (ruleViolation.getRule().equals(rule)) {
                    entry.getValue().remove(ruleViolation);
                }
            }
            if (entry.getValue().isEmpty()) {
                mapToClear.remove(entry.getKey());
            }
        }
    }

    public void removeAllRuleViolationsForRule(Rule rule) {
        if (null != instanceByType) {
            removeAllRuleViolationsForRule(rule, instanceByType);
        }
        if (null != instanceByClass) {
            removeAllRuleViolationsForRule(rule, instanceByClass);
        }
//        session.resultChanged(null, this);
    }

    public Map<Object, Collection<IRuleViolation>> getInstanceByType() {
        if (null == instanceByType) {
            Map<Object, Collection<IRuleViolation>> tempInstanceByType = new TreeMap<Object, Collection<IRuleViolation>>();

            Iterator<IRuleViolation> ruleViolationIterator = report.iterator();

            while (ruleViolationIterator.hasNext()) {
                IRuleViolation ruleViolation = ruleViolationIterator.next();
                CategoryKey categoryKey = new CategoryKey(ruleViolation);
                Collection<IRuleViolation> ruleViolations = tempInstanceByType.get(categoryKey);

                if (null == ruleViolations) {
                    ruleViolations = new ArrayList<IRuleViolation>();
                    tempInstanceByType.put(categoryKey, ruleViolations);
                }

                ruleViolations.add(ruleViolation);
            }
            instanceByType = tempInstanceByType;
        }

        return instanceByType;
    }

    public Map<Object, Collection<IRuleViolation>> getInstanceByClass() {
        if (null == instanceByClass) {
            instanceByClass = new TreeMap<Object, Collection<IRuleViolation>>();

            Iterator<IRuleViolation> ruleViolationIterator = report.iterator();

            while (ruleViolationIterator.hasNext()) {
                IRuleViolation ruleViolation = ruleViolationIterator.next();
                ClassKey classKey = new ClassKey(ruleViolation);
                Collection<IRuleViolation> ruleViolations = instanceByClass.get(classKey);

                if (null == ruleViolations) {
                    ruleViolations = new ArrayList<IRuleViolation>();
                    instanceByClass.put(classKey, ruleViolations);
                }

                ruleViolations.add(ruleViolation);
            }
        }

        return instanceByClass;
    }

    public Map<Object, Collection<IRuleViolation>> getInstanceByPackage() {
        if (null == instanceByPackage) {
            instanceByPackage = new TreeMap<Object, Collection<IRuleViolation>>();

            Iterator<IRuleViolation> ruleViolationIterator = report.iterator();

            while (ruleViolationIterator.hasNext()) {
                IRuleViolation ruleViolation = ruleViolationIterator.next();
                PackageKey packageKey = new PackageKey(ruleViolation);
                Collection<IRuleViolation> ruleViolations = instanceByPackage.get(packageKey);

                if (null == ruleViolations) {
                    ruleViolations = new ArrayList<IRuleViolation>();
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

    public long getCodeDefectCountSum() {
        return getBugCount();
    }

    public long getCodeDefactCount(CodeDefectSeverity severity) {
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

    public abstract static class DisplayableKey implements Comparable {

        public abstract String getDisplayName();

        @Override
        public final boolean equals(Object object) {
            if (object instanceof DisplayableKey) {
                return ((DisplayableKey) object).getDisplayName().equals(this.getDisplayName());
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return this.getDisplayName().hashCode();
        }

        public final int compareTo(Object object) {
            if (object instanceof DisplayableKey) {
                return this.getDisplayName().compareTo(((DisplayableKey) object).getDisplayName());
            }
            throw new IllegalArgumentException("Can't be compared to " + object.getClass());
        }
    }

    public static class ClassKey extends DisplayableKey {

        private final String className;
        private final FileObject fileObject;

        public ClassKey(final IRuleViolation ruleViolation) {
            this.className = (0 == ruleViolation.getPackageName().length() ? "" : (ruleViolation.getPackageName() + ".")) +
                    ((null == ruleViolation.getClassName() || ruleViolation.getClassName().length() == 0)
                    ? ruleViolation.getFilename() : ruleViolation.getClassName());
            this.fileObject = RuleViolationAnnotationProcessor.findFileObjectForAnnotatedClass(className);
        }

        public ClassKey(String className) {
            this.className = className;
            this.fileObject = RuleViolationAnnotationProcessor.findFileObjectForAnnotatedClass(className);
        }

        public String getDisplayName() {
            return this.className;
        }

        public String getClassName() {
            return this.className;
        }

        public FileObject getFileObject() {
            return this.fileObject;
        }
    }

    public static class PackageKey extends DisplayableKey {

        private final String packageName;

        public PackageKey(final IRuleViolation ruleViolation) {
            this.packageName = 0 == ruleViolation.getPackageName().length() ? "<Default Package>" : ruleViolation.getPackageName();
        }

        public String getDisplayName() {
            return this.packageName;
        }
    }

    public static class CategoryKey extends DisplayableKey {

        private final IRuleViolation bugPattern;

        public CategoryKey(final IRuleViolation bugPattern) {
            this.bugPattern = bugPattern;
        }

        public String getDisplayName() {
            return this.bugPattern.getRule().getName();
        }

        public String getDescription() {
            return bugPattern.getDescription();
        }
    }
}
