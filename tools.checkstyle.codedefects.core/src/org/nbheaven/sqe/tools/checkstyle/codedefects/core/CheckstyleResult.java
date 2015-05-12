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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.annotations.AuditEventAnnotationProcessor;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sven
 */
public class CheckstyleResult implements QualityResult, AuditListener, Lookup.Provider, QualityResultStatistic {

    public enum Mode {

        CLASS("HINT_VIEW_BY_CLASS", "org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/class.gif") {

            public Map<Object, Collection<AuditEvent>> getInstanceList(CheckstyleResult result) {
                return result.getInstanceByClass();
            }
        },
        PACKAGE("HINT_VIEW_BY_PACKAGE", "org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/package.gif") {

            public Map<Object, Collection<AuditEvent>> getInstanceList(CheckstyleResult result) {
                return result.getInstanceByPackage();
            }
        },
        TYPE("HINT_VIEW_BY_PACKAGE", "org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/checkstyle.png") {

            public Map<Object, Collection<AuditEvent>> getInstanceList(CheckstyleResult result) {
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

        public abstract Map<Object, Collection<AuditEvent>> getInstanceList(final CheckstyleResult result);
    }
    private Collection<AuditEvent> auditEvents = new LinkedList<AuditEvent>();
    private Map<Object, Collection<AuditEvent>> instanceBySource;
    private Map<Object, Collection<AuditEvent>> instanceByClass;
    private Map<Object, Collection<AuditEvent>> instanceByType;
    private Map<Object, Collection<AuditEvent>> instanceByPackage;
//    private Map<Object, List<RuleViolation>> instanceByType = null;
    private long bugCount = 0;
//    private Report report;
    private Lookup lookup;
    private final Project project;

    /**
     * Creates a new instance of CheckstyleResult
     */
    public CheckstyleResult(Project project) {
        lookup = Lookups.fixed(new Object[]{this});
        this.project = project;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public Map<Object, Collection<AuditEvent>> getInstanceBySource() {
        if (null == instanceBySource) {
            instanceBySource = new TreeMap<Object, Collection<AuditEvent>>();
            for (AuditEvent auditEvent : auditEvents) {
                Collection<AuditEvent> events = instanceBySource.get(auditEvent.getSourceName());
                if (null == events) {
                    events = new ArrayList<AuditEvent>();
                    instanceBySource.put(auditEvent.getSourceName(), events);
                }
                events.add(auditEvent);
            }
        }
        return instanceBySource;
    }

    public Map<Object, Collection<AuditEvent>> getInstanceByClass() {
        if (null == instanceByClass) {
            instanceByClass = new TreeMap<Object, Collection<AuditEvent>>();
            for (AuditEvent auditEvent : auditEvents) {
                if (auditEvent.getFileName().endsWith(".java")) {
                    FileObject file = AuditEventAnnotationProcessor.getFileObjectForAuditEvent(auditEvent, project);
                    if (file != null) {
                        ClassKey key = new ClassKey(file);
                        Collection<AuditEvent> events = instanceByClass.get(key);
                        if (null == events) {
                            events = new ArrayList<AuditEvent>();
                            instanceByClass.put(key, events);
                        }
                        events.add(auditEvent);
                    }
                }
            }
        }
        return instanceByClass;
    }

    public Map<Object, Collection<AuditEvent>> getInstanceByPackage() {
        if (null == instanceByPackage) {
            instanceByPackage = new TreeMap<Object, Collection<AuditEvent>>();
            for (AuditEvent auditEvent : auditEvents) {
                if (auditEvent.getFileName().endsWith(".java")) {
                    PackageKey key = new PackageKey(auditEvent);
                    Collection<AuditEvent> events = instanceByPackage.get(key);
                    if (null == events) {
                        events = new ArrayList<AuditEvent>();
                        instanceByPackage.put(key, events);
                    }
                    events.add(auditEvent);
                }
            }
        }
        return instanceByPackage;
    }

    public Map<Object, Collection<AuditEvent>> getInstanceByType() {
        if (null == instanceByType) {
            instanceByType = new TreeMap<Object, Collection<AuditEvent>>();
            for (AuditEvent auditEvent : auditEvents) {
                if (auditEvent.getFileName().endsWith(".java")) {
                    CategoryKey key = new CategoryKey(auditEvent);
                    Collection<AuditEvent> events = instanceByType.get(key);
                    if (null == events) {
                        events = new ArrayList<AuditEvent>();
                        instanceByType.put(key, events);
                    }
                    events.add(auditEvent);
                }
            }
        }
        return instanceByType;
    }

    public long getBugCount() {
        return bugCount;
    }

    public void addError(AuditEvent aEvt) {
//        System.out.println("error" + aEvt.getFileName() + ":" + aEvt.getLine() + "@" + aEvt.getColumn() + " Msg: " + aEvt.getMessage() + " Source: " + aEvt.getSourceName());
        auditEvents.add(aEvt);
        bugCount++;
    }

    public void addException(AuditEvent aEvt, Throwable aThrowable) {
//        System.out.println("exception" + aEvt);
//        aThrowable.printStackTrace();
    }

    public void auditFinished(AuditEvent aEvt) {
//        System.out.println("audit finished" + aEvt);
    }

    public void auditStarted(AuditEvent aEvt) {
//        System.out.println("audit started" + aEvt);
    }

    public void fileFinished(AuditEvent aEvt) {
//        System.out.println("file finished" + aEvt);
    }

    public void fileStarted(AuditEvent aEvt) {
//        System.out.println("file started" + aEvt);
    }

    public long getCodeDefectCountSum() {
        return getBugCount();
    }

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

    public abstract static class DisplayableKey implements Comparable<DisplayableKey> {

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

        public final int compareTo(DisplayableKey object) {
            return this.getDisplayName().compareTo(object.getDisplayName());
        }
    }

    public static class PackageKey extends DisplayableKey {

        private AuditEvent auditEvent;

        public PackageKey(AuditEvent auditEvent) {
            this.auditEvent = auditEvent;
        }

        public String getDisplayName() {
            return this.auditEvent.getFileName();
        }
    }

    public static class ClassKey extends DisplayableKey {

        private final String className;
        private final FileObject fileObject;

        public ClassKey(FileObject fileObject) {
            this.fileObject = fileObject;
            // XXX this is almost surely wrong but I do not know what the intent was:
            className = fileObject.getPath();
        }

        public String getDisplayName() {
            return className;
        }

        public FileObject getFileObject() {
            return this.fileObject;
        }
    }

    public static class CategoryKey extends DisplayableKey {

        private final AuditEvent auditEvent;
        private final String displayName;

        public CategoryKey(AuditEvent auditEvent) {
            this.auditEvent = auditEvent;
            this.displayName = this.auditEvent.getSourceName().substring(this.auditEvent.getSourceName().lastIndexOf('.') + 1);
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getDescription() {
            return auditEvent.getMessage();
        }
    }
}
