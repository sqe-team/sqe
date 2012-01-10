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
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleSession;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
public final class CheckstyleTaskProvider extends PushTaskScanner {
        
    public CheckstyleTaskProvider() {
        super( "Checkstyle", "Checkstyle found Errors", null);
    }    
    
    public synchronized void setScope(TaskScanningScope taskScanningScope, Callback callback) {        
        if (taskScanningScope == null || callback == null)
            return ;
        
        for (FileObject file : taskScanningScope.getLookup().lookupAll(FileObject.class)) {
            Project project = FileOwnerQuery.getOwner(file);
            if (null != project && null != project.getLookup().lookup(CheckstyleSession.class) && null!=JavaSource.forFileObject(file)) {
                CheckstyleResult result = getResult(file);
                if (result == null) {
                    continue;
                }
                Map<Object, Collection<AuditEvent>> instanceByClass = result.getInstanceByClass();
                CheckstyleResult.ClassKey key = new CheckstyleResult.ClassKey(file);
                Collection<AuditEvent> auditEvents = instanceByClass.get(key);
                if (auditEvents != null) { // SQE-57
                    callback.setTasks(file, new LinkedList<Task>(getTasks(auditEvents, file)));
                }
            }
        }
        
        for (Project project : taskScanningScope.getLookup().lookupAll(Project.class)) {
            if(null != project.getLookup().lookup(CheckstyleSession.class)) {
                CheckstyleResult result = getResult(project);
                if (result == null) {
                    continue;
                }
                List<Task> tasks = new LinkedList<Task>();
                for (Map.Entry<Object, Collection<AuditEvent>> classEntry: result.getInstanceByClass().entrySet()) {
                    CheckstyleResult.ClassKey key = (CheckstyleResult.ClassKey) classEntry.getKey();
                    tasks.addAll(getTasks(classEntry.getValue(), key.getFileObject()));
                }            
                callback.setTasks(project.getProjectDirectory(), tasks);
            }
        }    
        
    }

    private List<Task> getTasks(Collection<AuditEvent> auditevents, FileObject file) {
        List<Task> tasks = new LinkedList<Task>();
        for(AuditEvent auditEvent: auditevents) {
            tasks.add(Task.create(file, "sqe-tasklist-checkstyle", auditEvent.getMessage(), auditEvent.getLine()));                                
        }
        return tasks;
    } 
    
    private CheckstyleResult getResult(FileObject fileObject) {
        return getResult(FileOwnerQuery.getOwner(fileObject));        
    }
    
    private CheckstyleResult getResult(Project project) {
        CheckstyleSession qualitySession = project.getLookup().lookup(CheckstyleSession.class);
        if (qualitySession == null) {
            return null;
        }
        CheckstyleResult result = qualitySession.getResult();
        if (null == result) {
            result = qualitySession.computeResultAndWait();
        }            
        return result;
    }
    
    private Collection<String> getClasses (final FileObject fo) {
        if (fo == null || !fo.isValid() || fo.isVirtual()) {
            throw new IllegalArgumentException();
        }
        final JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            throw new IllegalArgumentException();
        }
        try {
            final Collection<String> result = new ArrayList<String>();
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                public void run(final CompilationController control) throws Exception {
                    if (control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) >= 0) {                        
                        new TreePathScanner<Void, Void>() {

                            private void recurseType(TypeElement te, String prefix) {
                                for (TypeElement type : ElementFilter.typesIn(te.getEnclosedElements())) {
                                    result.add(prefix + type.getSimpleName().toString());
                                    recurseType(type, prefix + type.getSimpleName() + "$");
                                }                                
                            }
                            
                            @Override
                            public Void visitClass(ClassTree t, Void v) {
                                Element el = control.getTrees().getElement(getCurrentPath());

                                TypeElement te = (TypeElement) el;
                                String outerFQN = te.getQualifiedName().toString();
                                result.add(outerFQN);
                                recurseType(te, outerFQN + "$");
                                return null;
                            }
                        }.scan(control.getCompilationUnit(), null);
                    }
                }
                                
                public void cancel() {}
                
            }, true);
            return result;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return Collections.<String>emptyList();
        }
    }}
