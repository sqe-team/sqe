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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.internal;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult.CategoryKey;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult.ClassKey;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult.PackageKey;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.annotations.AuditEventAnnotationProcessor;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Sven Reimers
 */
public final class CheckstyleResultImpl implements CheckstyleResult {

    private final Collection<AuditEvent> auditEvents = new LinkedList<>();
    private Map<String, Collection<AuditEvent>> instanceBySource;
    private Map<ClassKey, Collection<AuditEvent>> instanceByClass;
    private Map<CategoryKey, Collection<AuditEvent>> instanceByType;
    private Map<PackageKey, Collection<AuditEvent>> instanceByPackage;
//    private Map<Object, List<RuleViolation>> instanceByType = null;
    private long bugCount = 0;
//    private Report report;
    private final Lookup lookup;
    private final Project project;

    /**
     * Creates a new instance of CheckstyleResult
     */
    public CheckstyleResultImpl(Project project) {
        lookup = Lookups.fixed(new Object[]{this});
        this.project = project;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public Map<String, Collection<AuditEvent>> getInstanceBySource() {
        if (null == instanceBySource) {
            instanceBySource = new TreeMap<>();
            for (AuditEvent auditEvent : auditEvents) {
                Collection<AuditEvent> events = instanceBySource.get(auditEvent.getSourceName());
                if (null == events) {
                    events = new ArrayList<>();
                    instanceBySource.put(auditEvent.getSourceName(), events);
                }
                events.add(auditEvent);
            }
        }
        return instanceBySource;
    }

    @Override
    public Map<ClassKey, Collection<AuditEvent>> getInstanceByClass() {
        if (null == instanceByClass) {
            instanceByClass = new TreeMap<>();
            for (AuditEvent auditEvent : auditEvents) {
                if (auditEvent.getFileName().endsWith(".java")) {
                    FileObject file = AuditEventAnnotationProcessor.getFileObjectForAuditEvent(auditEvent, project);
                    if (file != null) {
                        ClassKey key = new ClassKey(project, file);
                        Collection<AuditEvent> events = instanceByClass.get(key);
                        if (null == events) {
                            events = new ArrayList<>();
                            instanceByClass.put(key, events);
                        }
                        events.add(auditEvent);
                    }
                }
            }
        }
        return instanceByClass;
    }

    @Override
    public Map<PackageKey, Collection<AuditEvent>> getInstanceByPackage() {
        if (null == instanceByPackage) {
            instanceByPackage = new TreeMap<>();
            for (AuditEvent auditEvent : auditEvents) {
                if (auditEvent.getFileName().endsWith(".java")) {
                    PackageKey key = new PackageKey(project, auditEvent);
                    Collection<AuditEvent> events = instanceByPackage.get(key);
                    if (null == events) {
                        events = new ArrayList<>();
                        instanceByPackage.put(key, events);
                    }
                    events.add(auditEvent);
                }
            }
        }
        return instanceByPackage;
    }

    @Override
    public Map<CategoryKey, Collection<AuditEvent>> getInstanceByType() {
        if (null == instanceByType) {
            instanceByType = new TreeMap<>();
            for (AuditEvent auditEvent : auditEvents) {
                if (auditEvent.getFileName().endsWith(".java")) {
                    CategoryKey key = new CategoryKey(project, auditEvent);
                    Collection<AuditEvent> events = instanceByType.get(key);
                    if (null == events) {
                        events = new ArrayList<>();
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

    @Override
    public void addError(AuditEvent aEvt) {
//        System.out.println("error" + aEvt.getFileName() + ":" + aEvt.getLine() + "@" + aEvt.getColumn() + " Msg: " + aEvt.getMessage() + " Source: " + aEvt.getSourceName());
        auditEvents.add(aEvt);
        bugCount++;
    }

    @Override
    public void addException(AuditEvent aEvt, Throwable aThrowable) {
//        System.out.println("exception" + aEvt);
//        aThrowable.printStackTrace();
    }

    @Override
    public void auditFinished(AuditEvent aEvt) {
//        System.out.println("audit finished" + aEvt);
    }

    @Override
    public void auditStarted(AuditEvent aEvt) {
//        System.out.println("audit started" + aEvt);
    }

    @Override
    public void fileFinished(AuditEvent aEvt) {
//        System.out.println("file finished" + aEvt);
    }

    @Override
    public void fileStarted(AuditEvent aEvt) {
//        System.out.println("file started" + aEvt);
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
