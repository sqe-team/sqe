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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.internal.AuditEventSupport;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Sven Reimers
 */
public interface CheckstyleResult extends QualityResult, AuditListener, Lookup.Provider, QualityResultStatistic {

    public enum Mode {

        CLASS("HINT_VIEW_BY_CLASS", "org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/class.gif") {

            @Override
            public Map<ClassKey, Collection<AuditEvent>> getInstanceList(CheckstyleResult result) {
                return result.getInstanceByClass();
            }
        },
        PACKAGE("HINT_VIEW_BY_PACKAGE", "org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/package.gif") {

            @Override
            public Map<PackageKey, Collection<AuditEvent>> getInstanceList(CheckstyleResult result) {
                return result.getInstanceByPackage();
            }
        },
        TYPE("HINT_VIEW_BY_TYPE", "org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/checkstyle.png") {

            @Override
            public Map<CategoryKey, Collection<AuditEvent>> getInstanceList(CheckstyleResult result) {
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

        public abstract Map<? extends Object, Collection<AuditEvent>> getInstanceList(final CheckstyleResult result);
    }

    public Map<String, Collection<AuditEvent>> getInstanceBySource();

    public Map<ClassKey, Collection<AuditEvent>> getInstanceByClass();

    public Map<PackageKey, Collection<AuditEvent>> getInstanceByPackage();

    public Map<CategoryKey, Collection<AuditEvent>> getInstanceByType();

    public abstract static class DisplayableKey implements Comparable<DisplayableKey> {

        DisplayableKey() {
        }

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

        @Override
        public final int compareTo(DisplayableKey object) {
            return this.getDisplayName().compareTo(object.getDisplayName());
        }
    }

    public static final class PackageKey extends DisplayableKey {

        private final AuditEvent auditEvent;
        private final String packageName;

        public PackageKey(Project project, AuditEvent auditEvent) {
            this.auditEvent = auditEvent;
//            this.packageName = new File(this.auditEvent.getFileName()).getParent();
            this.packageName = new File(AuditEventSupport.getRelativeProjectFilePath(project, auditEvent)).getParent();
        }

        @Override
        public String getDisplayName() {
            return packageName;
        }
    }

    public static final class ClassKey extends DisplayableKey {

        private final String className;
        private final FileObject fileObject;

        public ClassKey(Project project, FileObject fileObject) {
            this.fileObject = fileObject;
            // XXX this is almost surely wrong but I do not know what the intent was:
            className = AuditEventSupport.getRelativeProjectFilePath(project, fileObject);
        }

        @Override
        public String getDisplayName() {
            return className;
        }

        public FileObject getFileObject() {
            return this.fileObject;
        }
    }

    public static final class CategoryKey extends DisplayableKey {

        private final AuditEvent auditEvent;
        private final String displayName;

        public CategoryKey(Project project, AuditEvent auditEvent) {
            this.auditEvent = auditEvent;
            this.displayName = this.auditEvent.getSourceName().substring(this.auditEvent.getSourceName().lastIndexOf('.') + 1);
        }

        @Override
        public String getDisplayName() {
            return this.displayName;
        }

        public String getDescription() {
            return auditEvent.getMessage();
        }
    }
}
