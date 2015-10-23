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

import java.util.Collection;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.Rule;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.tools.pmd.codedefects.core.annotations.RuleViolationAnnotationProcessor;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Sven Reimers
 */
public interface PMDResult extends QualityResult, Lookup.Provider, QualityResultStatistic {

    public enum Mode {

        CLASS("HINT_VIEW_BY_CLASS", "org/nbheaven/sqe/tools/pmd/codedefects/core/resources/class.gif") {

            @Override
            public Map<ClassKey, Collection<RuleViolation>> getInstanceList(final PMDResult result) {
                return result.getInstanceByClass();
            }
        },
        PACKAGE("HINT_VIEW_BY_PACKAGE", "org/nbheaven/sqe/tools/pmd/codedefects/core/resources/package.gif") {

            @Override
            public Map<PackageKey, Collection<RuleViolation>> getInstanceList(final PMDResult result) {
                return result.getInstanceByPackage();
            }
        },
        TYPE("HINT_VIEW_BY_CATEGORY", "org/nbheaven/sqe/tools/pmd/codedefects/core/resources/pmd.png") {

            @Override
            public Map<CategoryKey, Collection<RuleViolation>> getInstanceList(final PMDResult result) {
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

        public abstract Map<?, Collection<RuleViolation>> getInstanceList(final PMDResult result);
    }

    public void removeAllRuleViolationsForRule(Rule rule);

    public Map<CategoryKey, Collection<RuleViolation>> getInstanceByType();

    public Map<ClassKey, Collection<RuleViolation>> getInstanceByClass();

    public Map<PackageKey, Collection<RuleViolation>> getInstanceByPackage();

    public abstract static class DisplayableKey implements Comparable<DisplayableKey> {

        public abstract String getDisplayName();

        DisplayableKey() {
        }

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

        @Override
        public final int compareTo(DisplayableKey object) {
            return this.getDisplayName().compareTo(object.getDisplayName());
        }
    }

    public static final class ClassKey extends DisplayableKey {

        private final String className;
        private final FileObject fileObject;

        public ClassKey(final RuleViolation ruleViolation) {
            this.className = (0 == ruleViolation.getPackageName().length() ? "" : (ruleViolation.getPackageName() + "."))
                    + ((null == ruleViolation.getClassName() || ruleViolation.getClassName().length() == 0)
                            ? ruleViolation.getFilename() : ruleViolation.getClassName());
            this.fileObject = RuleViolationAnnotationProcessor.findFileObjectForAnnotatedClass(className);
        }

        public ClassKey(String className) {
            this.className = className;
            this.fileObject = RuleViolationAnnotationProcessor.findFileObjectForAnnotatedClass(className);
        }

        @Override
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

    public static final class PackageKey extends DisplayableKey {

        private final String packageName;

        public PackageKey(final RuleViolation ruleViolation) {
            this.packageName = 0 == ruleViolation.getPackageName().length() ? "<Default Package>" : ruleViolation.getPackageName();
        }

        @Override
        public String getDisplayName() {
            return this.packageName;
        }
    }

    public static final class CategoryKey extends DisplayableKey {

        private final RuleViolation bugPattern;

        public CategoryKey(final RuleViolation bugPattern) {
            this.bugPattern = bugPattern;
        }

        @Override
        public String getDisplayName() {
            return this.bugPattern.getRule().getName();
        }

        public String getDescription() {
            return bugPattern.getDescription();
        }
    }
}
