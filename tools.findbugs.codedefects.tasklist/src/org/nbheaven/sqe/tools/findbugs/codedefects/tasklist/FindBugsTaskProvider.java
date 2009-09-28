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
package org.nbheaven.sqe.tools.findbugs.codedefects.tasklist;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;
import org.nbheaven.sqe.core.java.search.JavaElement;
import org.nbheaven.sqe.core.java.search.SearchUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.nbheaven.sqe.core.java.search.ClassElementDescriptor;
import org.nbheaven.sqe.core.java.search.MethodElementDescriptor;
import org.nbheaven.sqe.core.java.search.VariableElementDescriptor;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.MethodElementDescriptorImpl;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.ClassElementDescriptorImpl;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.VariableElementDescriptorImpl;

/**
 *
 * @author Sven Reimers
 */
public class FindBugsTaskProvider extends PushTaskScanner {

    public FindBugsTaskProvider() {
        super("FindBugs", "FindBugs found Bugs", null);
    }

    public synchronized void setScope(TaskScanningScope taskScanningScope, Callback callback) {
        if (taskScanningScope == null || callback == null) {
            return;
        }

        for (FileObject file : taskScanningScope.getLookup().lookupAll(FileObject.class)) {
            Project project = FileOwnerQuery.getOwner(file);
            if (null != project && null != project.getLookup().lookup(FindBugsSession.class) && null != JavaSource.forFileObject(file)) {
                FindBugsResult result = getResult(file);
                Map<Object, Collection<BugInstance>> instanceByClass = result.getInstanceByClass(true);
                Collection<String> classes = SearchUtilities.getFQNClassNames(file);
                List<Task> tasks = new LinkedList<Task>();
                for (String className : classes) {
                    for (Object key : instanceByClass.keySet()) {
                        FindBugsResult.ClassKey classKey = (FindBugsResult.ClassKey) key;
                        if (classKey.getDisplayName().equals(className)) {
                            Collection<BugInstance> bugs = instanceByClass.get(classKey);
                            tasks.addAll(getTasks(bugs, file));
                        }
                    }
                }
                callback.setTasks(file, tasks);
            }
        }

        for (Project project : taskScanningScope.getLookup().lookupAll(Project.class)) {
            if (null != project.getLookup().lookup(FindBugsSession.class)) {
                FindBugsResult result = getResult(project);
                List<Task> tasks = new LinkedList<Task>();
                for (Map.Entry<Object, Collection<BugInstance>> classEntry : result.getInstanceByClass(true).entrySet()) {
                    tasks.addAll(getTasks(classEntry.getValue(), ((FindBugsResult.ClassKey)classEntry.getKey()).getFileObject()));
                }
                callback.setTasks(project.getProjectDirectory(), tasks);
            }
        }

    }

    private List<Task> getTasks(Collection<BugInstance> bugs, final FileObject file) {
        if (file == null) {
            return Collections.emptyList();
        }
        List<Task> tasks = new LinkedList<Task>();
        for (final BugInstance bugInstance : bugs) {
            SourceLineAnnotation sourceLineAnnotation = null;
            // Highest priority: return the first top level source line annotation
            for (Iterator<BugAnnotation> annotationIterator = bugInstance.annotationIterator(); annotationIterator.hasNext();) {
                BugAnnotation annotation = annotationIterator.next();
                if (annotation instanceof SourceLineAnnotation) {
                    sourceLineAnnotation = (SourceLineAnnotation) annotation;
                    break;
                }
            }

            if (null == sourceLineAnnotation) {
                tasks.add(Task.create(file, "sqe-tasklist-findbugs", bugInstance.getMessage(), new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        Project project = FileOwnerQuery.getOwner(file);
                        FieldAnnotation fieldAnnotation = bugInstance.getPrimaryField();
                        if (null != fieldAnnotation) {
                            VariableElementDescriptor desc = new VariableElementDescriptorImpl(bugInstance.getPrimaryClass(), fieldAnnotation, project);
                            JavaElement findFieldElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findVariableElement(desc);
                            if (findFieldElement != null) {
                                findFieldElement.open();
                            }
                            return;
                        }
                        MethodAnnotation methodAnnotation = bugInstance.getPrimaryMethod();
                        if (null != methodAnnotation) {
                            MethodElementDescriptor desc = new MethodElementDescriptorImpl(bugInstance.getPrimaryClass(), methodAnnotation, project);
                            JavaElement findMethodElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findMethodElement(desc);
                            if (findMethodElement != null) {
                                findMethodElement.open();
                            }
                            return;
                        }
                        ClassAnnotation classAnnotation = bugInstance.getPrimaryClass();
                        if (null != classAnnotation) {
                            ClassElementDescriptor desc = new ClassElementDescriptorImpl(classAnnotation, project);
                            JavaElement findClassElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findClassElement(desc);
                            if (findClassElement != null) {
                                findClassElement.open();
                            }
                            return;
                        }
                    }
                }));
            } else {
                tasks.add(Task.create(file, "sqe-tasklist-findbugs", bugInstance.getMessage(), bugInstance.getPrimarySourceLineAnnotation().getStartLine()));
            }
        }
        return tasks;
    }

    private FindBugsResult getResult(FileObject fileObject) {
        return getResult(FileOwnerQuery.getOwner(fileObject));
    }

    private FindBugsResult getResult(Project project) {
        FindBugsSession qualitySession = project.getLookup().lookup(FindBugsSession.class);
        FindBugsResult result = qualitySession.getResult();
        if (null == result) {
            result = qualitySession.computeResultAndWait();
        }
        return result;
    }
}
