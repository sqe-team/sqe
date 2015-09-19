/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbheaven.sqe.tools.findbugs.codedefects.hints;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.text.Document;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.core.java.search.ClassElementDescriptor;
import org.nbheaven.sqe.core.java.search.JavaElement;
import org.nbheaven.sqe.core.java.search.MethodElementDescriptor;
import org.nbheaven.sqe.core.java.search.SearchUtilities;
import org.nbheaven.sqe.core.java.search.VariableElementDescriptor;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.ClassElementDescriptorImpl;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.MethodElementDescriptorImpl;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.VariableElementDescriptorImpl;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author fvo
 */
final class FindBugsHintTask implements CancellableTask<CompilationInfo> {

    private static final RequestProcessor HINT_PROCESSOR = new RequestProcessor("FindBugs-Hint-Processor", 1);
    private volatile List<ErrorDescription> errors;

    public FindBugsHintTask() {
    }

    @Override
    public void cancel() {
        // TODO kill this
    }

    @Override
    public synchronized void run(final CompilationInfo compilationInfo) throws Exception {
        final FileObject fo = compilationInfo.getFileObject();
        if (null != fo) {
            if (null == errors) {
                System.out.println("FindBugsHintTask: (calc) " + System.identityHashCode(fo));
                final Document document = compilationInfo.getDocument();
                if (null != document) {
                    HINT_PROCESSOR.post(() -> {
                        try {
                            errors = computeErrors(fo, document);
                            FindBugsHintTaskFactory.rescheduleFile(fo);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    });
                } else {
                    errors = Collections.emptyList();
                }
            } else {
                System.out.println("FindBugsHintTask: (show) " + System.identityHashCode(fo));
                HintsController.setErrors(fo, getClass().getName(), errors);
                errors = null;
            }
        }
    }

    private static List<ErrorDescription> computeErrors(FileObject fileObject, Document document) throws Exception {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (null != project) {
            FindBugsSession session = project.getLookup().lookup(FindBugsSession.class);
            if (null != session) {
                if (SQECodedefectProperties.isQualityProviderActive(project, session.getProvider())) {

                    List<ErrorDescription> computedErrors = new LinkedList<>();
                    FindBugsResult result = session.computeResultAndWait(fileObject);

                    Map<FindBugsResult.ClassKey, Collection<BugInstance>> instanceByClass = result.getInstanceByClass(true);
                    Collection<String> classes = SearchUtilities.getFQNClassNames(fileObject);

                    classes.stream().forEach((className) -> {
                        instanceByClass.keySet().stream()
                                .filter((classKey) -> (classKey.getDisplayName().equals(className)))
                                .map((classKey) -> instanceByClass.get(classKey))
                                .map((bugs) -> getErrors(project, bugs, fileObject, document))
                                .flatMap(list -> list.stream())
                                .collect(Collectors.toList());

                    });
                    return computedErrors;
                }
            }
        }
        return Collections.emptyList();
    }

    private static List<ErrorDescription> getErrors(final Project project, Collection<BugInstance> bugs, final FileObject file, final Document document) {
        List<ErrorDescription> errorDescriptions = new LinkedList<>();
        for (final BugInstance bugInstance : bugs) {
            try {
                int line = 0;
                // Highest priority: return the first top level source line annotation
                // XXX why is this not using just one call to getPrimarySourceLineAnnotation? seems to work just as well, and always non-null
                for (BugAnnotation annotation : bugInstance.getAnnotations()) {
                    if (annotation instanceof SourceLineAnnotation) {
                        line = Math.max(1, bugInstance.getPrimarySourceLineAnnotation().getStartLine());
                        break;
                    }
                }
                JavaElement findElement = locateElement(bugInstance, project);
                if (line == 0) {
                    if (findElement != null) {
                        line = Math.max(1, findElement.getLine().getLineNumber() + 1);
                    }
                }
                if (line > 0) {
                    List<Fix> fixes = new ArrayList<>();
                    if (findElement != null) {
                        fixes.add(new SuppressWarningsFix(bugInstance.getType(), findElement.getHandle(), file));
                    }
                    fixes.add(new DisableDetectorFix(bugInstance, project));
                    errorDescriptions.add(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "[FindBugs] " + bugInstance.getAbridgedMessage(), fixes, document, line));
                }
            } catch (RuntimeException e) {
                Logger.getLogger(FindBugsHintTask.class.getName()).log(Level.INFO,
                        "INFO: Can''t create ErrorDescription for FindBugs bug instance: {0}",
                        bugInstance.getMessage());
            }
        }
        return errorDescriptions;
    }

    private static JavaElement locateElement(BugInstance bugInstance, Project project) {
        MethodAnnotation methodAnnotation = bugInstance.getPrimaryMethod();
        if (methodAnnotation != null) {
            MethodElementDescriptor desc = new MethodElementDescriptorImpl(bugInstance.getPrimaryClass(), methodAnnotation, project);
            JavaElement e = SearchUtilities.findMethodElement(desc);
            if (e != null) {
                return e;
            }
        }
        FieldAnnotation fieldAnnotation = bugInstance.getPrimaryField();
        if (fieldAnnotation != null) {
            VariableElementDescriptor desc = new VariableElementDescriptorImpl(bugInstance.getPrimaryClass(), fieldAnnotation, project);
            JavaElement e = SearchUtilities.findVariableElement(desc);
            if (e != null) {
                return e;
            }
        }
        ClassAnnotation classAnnotation = bugInstance.getPrimaryClass();
        if (classAnnotation != null) {
            ClassElementDescriptor desc = new ClassElementDescriptorImpl(classAnnotation, project);
            JavaElement e = SearchUtilities.findClassElement(desc);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

}
