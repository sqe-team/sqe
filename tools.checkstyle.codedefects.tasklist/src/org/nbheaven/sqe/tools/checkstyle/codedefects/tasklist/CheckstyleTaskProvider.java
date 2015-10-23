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
package org.nbheaven.sqe.tools.checkstyle.codedefects.tasklist;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.nbheaven.sqe.codedefects.core.spi.SQEUtilities;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult.ClassKey;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleSession;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Sven Reimers
 */
public final class CheckstyleTaskProvider extends PushTaskScanner {

    public CheckstyleTaskProvider() {
        super("Checkstyle", "Checkstyle found Errors", null);
    }

    @Override
    public synchronized void setScope(TaskScanningScope taskScanningScope, Callback callback) {
        if (taskScanningScope == null || callback == null) {
            return;
        }

        for (FileObject fileObject : taskScanningScope.getLookup().lookupAll(FileObject.class)) {
            if (SQECodedefectSupport.isQualityProviderEnabledForFileObject(fileObject, CheckstyleSession.class) && JavaSource.forFileObject(fileObject) != null) {
                CheckstyleResult result = getResult(fileObject);
                if (result == null) {
                    continue;
                }
                Project project = SQEProjectSupport.findProjectByFileObject(fileObject);
                Map<ClassKey, Collection<AuditEvent>> instanceByClass = result.getInstanceByClass();
                CheckstyleResult.ClassKey key = new CheckstyleResult.ClassKey(project, fileObject);
                Collection<AuditEvent> auditEvents = instanceByClass.get(key);
                if (auditEvents != null) { // SQE-57
                    callback.setTasks(fileObject, new LinkedList<>(getTasks(auditEvents, fileObject)));
                }
            }
        }

        for (Project project : taskScanningScope.getLookup().lookupAll(Project.class)) {
            CheckstyleResult result = getResult(project);
            if (result != null) {
                List<Task> tasks = new LinkedList<>();
                for (Map.Entry<ClassKey, Collection<AuditEvent>> classEntry : result.getInstanceByClass().entrySet()) {
                    tasks.addAll(getTasks(classEntry.getValue(), classEntry.getKey().getFileObject()));
                }
                callback.setTasks(project.getProjectDirectory(), tasks);
            }
        }
    }

    private List<Task> getTasks(Collection<AuditEvent> auditevents, FileObject file) {
        List<Task> tasks = new LinkedList<>();
        for (AuditEvent auditEvent : auditevents) {
            tasks.add(Task.create(file, "sqe-tasklist-checkstyle", auditEvent.getMessage(), auditEvent.getLine()));
        }
        return tasks;
    }

    private CheckstyleResult getResult(FileObject fileObject) {
        return getResult(FileOwnerQuery.getOwner(fileObject));
    }

    private CheckstyleResult getResult(Project project) {
        CheckstyleSession qualitySession = SQECodedefectSupport.retrieveSession(project, CheckstyleSession.class);

        CheckstyleResult result = null;
        if (qualitySession != null) {
            result = qualitySession.getResult();
            if (null == result) {
                result = qualitySession.computeResultAndWait();
            }

        }
        return result;
    }

//    private Collection<String> getClasses(final FileObject fo) {
//        if (fo == null || !fo.isValid() || fo.isVirtual()) {
//            throw new IllegalArgumentException();
//        }
//        final JavaSource js = JavaSource.forFileObject(fo);
//        if (js == null) {
//            throw new IllegalArgumentException();
//        }
//        try {
//            final Collection<String> result = new ArrayList<String>();
//            js.runUserActionTask(new CancellableTask<CompilationController>() {
//                @Override
//                public void run(final CompilationController control) throws Exception {
//                    if (control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) >= 0) {
//                        new TreePathScanner<Void, Void>() {
//
//                            private void recurseType(TypeElement te, String prefix) {
//                                for (TypeElement type : ElementFilter.typesIn(te.getEnclosedElements())) {
//                                    result.add(prefix + type.getSimpleName().toString());
//                                    recurseType(type, prefix + type.getSimpleName() + "$");
//                                }
//                            }
//
//                            @Override
//                            public Void visitClass(ClassTree t, Void v) {
//                                Element el = control.getTrees().getElement(getCurrentPath());
//
//                                TypeElement te = (TypeElement) el;
//                                String outerFQN = te.getQualifiedName().toString();
//                                result.add(outerFQN);
//                                recurseType(te, outerFQN + "$");
//                                return null;
//                            }
//                        }.scan(control.getCompilationUnit(), null);
//                    }
//                }
//
//                @Override
//                public void cancel() {
//                }
//
//            }, true);
//            return result;
//        } catch (IOException ioe) {
//            Exceptions.printStackTrace(ioe);
//            return Collections.<String>emptyList();
//        }
//    }
}
