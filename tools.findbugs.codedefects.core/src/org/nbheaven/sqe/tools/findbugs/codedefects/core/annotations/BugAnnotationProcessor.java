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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.annotations;

import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult;
import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import java.awt.EventQueue;
import java.util.Iterator;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.MethodElementDescriptorImpl;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.SQEAnnotationProcessor;
import org.nbheaven.sqe.core.java.search.ClassElementDescriptor;
import org.nbheaven.sqe.core.java.search.JavaElement;
import org.nbheaven.sqe.core.java.search.MethodElementDescriptor;
import org.nbheaven.sqe.core.java.utils.JavaSourceProvider;
import org.nbheaven.sqe.core.java.utils.TypeUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.Line.Set;
import java.util.Collection;
import java.util.Map;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.ClassElementDescriptorImpl;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.VariableElementDescriptorImpl;
import org.nbheaven.sqe.core.java.search.SearchUtilities;
import org.nbheaven.sqe.core.java.search.VariableElementDescriptor;

/**
 *
 * @author Sven Reimers
 */
public final class BugAnnotationProcessor implements SQEAnnotationProcessor {

    public static final SQEAnnotationProcessor INSTANCE = new BugAnnotationProcessor();

    /** Creates a new instance of BugAnnotationProcessor */
    private BugAnnotationProcessor() {
    }

    private static Line getLineForSourceAnnotation(DataObject dao, SourceLineAnnotation sourceLineAnnotation) {
        LineCookie cookie = dao.getCookie(LineCookie.class);
        Set lineset = cookie.getLineSet();
        int lineNum = sourceLineAnnotation.getStartLine();
        return lineset.getCurrent(lineNum - 1);
    }

    private static void annotate(final BugInstance bugInstance, final JavaElement javaElement, Project project) {
        if (javaElement == null) {
            return;
        }
        FindBugsAnnotation annotation = FindBugsAnnotation.createNewInstance(project);
        annotation.setErrorMessage(bugInstance.getMessage());
        Line line = javaElement.getLine();
        Part linePart = line.createPart(javaElement.getBeginColumn(), javaElement.getEndColumn());
        annotation.attach(linePart);
        line.addPropertyChangeListener(annotation);
    }

    private static void annotate(final BugInstance bugInstance, final Line line, Project project) {
        FindBugsAnnotation annotation = FindBugsAnnotation.createNewInstance(project);
        annotation.setErrorMessage(bugInstance.getMessage());
        annotation.attach(line);
        line.addPropertyChangeListener(annotation);
    }

    private static void openSourceFileAndAnnotate(final BugInstance bugInstance, final Line line, Project project) {
        annotate(bugInstance, line, project);
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            }
        });
    }

    public static void openSourceFile(final BugInstance bugInstance, final SourceLineAnnotation sourceLineAnnotation, final Project project) {
        FileObject fo = findFileObjectForAnnotatedClass(sourceLineAnnotation.getClassName(), project);
        if (fo == null) {
            return;
        }
        try {
            DataObject dao = DataObject.find(fo);
            Line line = getLineForSourceAnnotation(dao, sourceLineAnnotation);
            openSourceFileAndAnnotate(bugInstance, line, project);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    public static void openSourceFile(final BugInstance bugInstance, final Project project) {
        SourceLineAnnotation sourceLineAnnotation = bugInstance.getPrimarySourceLineAnnotation();
        if (null != sourceLineAnnotation) {
            if (sourceLineAnnotation.getStartLine() > 0) {
                openSourceFile(bugInstance, sourceLineAnnotation, project);
                return;
            }
        }
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

    public static void openSourceFile(final BugInstance bugInstance, final FieldAnnotation fieldAnnotation, final Project project) {
        VariableElementDescriptor desc = new VariableElementDescriptorImpl(bugInstance.getPrimaryClass(), fieldAnnotation, project);
        JavaElement findMethodElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findVariableElement(desc);
        if (findMethodElement != null) {
            findMethodElement.open();
        }
    }

    public static void openSourceFile(final BugInstance bugInstance, final MethodAnnotation methodAnnotation, final Project project) {
        MethodElementDescriptor desc = new MethodElementDescriptorImpl(bugInstance.getPrimaryClass(), methodAnnotation, project);
        JavaElement findMethodElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findMethodElement(desc);
        if (findMethodElement != null) {
            findMethodElement.open();
        }
    }

    public static void openSourceFile(final BugInstance bugInstance, final ClassAnnotation classAnnotation, final Project project) {
        ClassElementDescriptor desc = new ClassElementDescriptorImpl(classAnnotation, project);
        JavaElement findClassElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findClassElement(desc);
        if (findClassElement != null) {
            findClassElement.open();
        }
    }

    public static FileObject findFileObjectForAnnotatedClass(final String className, final Project project) {
        String javaFileName = className;

        JavaSourceProvider javaSourceProvider = TypeUtilities.getJavaTypeElement(javaFileName, project);
        if (null == javaSourceProvider) {
            return null;
        }
        return javaSourceProvider.getFileObject();
    }

    public void annotateSourceFile(final JavaSource javaSource, final Project project, QualityResult qualityResult) {
        if (null == qualityResult) {
            return;
        }
        assert qualityResult instanceof FindBugsResult : "Illegal session passed to AnnotationProcessor";

        final FindBugsResult result = (FindBugsResult) qualityResult;

        FileObject fileObject = javaSource.getFileObjects().iterator().next();
        Collection<String> fqnClassNames = SearchUtilities.getFQNClassNames(fileObject);
        for (String fqnClassName : fqnClassNames) {
            annotateClass(fqnClassName, fileObject, project, result);
        }
    }

    public void clearAllAnnotations(Project project) {
        FindBugsAnnotation.clearAll(project);
    }

    private void annotateClass(String className, FileObject fileObject, Project project, FindBugsResult result) {
        Map<Object, Collection<BugInstance>> instanceMap = result.getInstanceByClass(true);
        FindBugsResult.StringKey key = new FindBugsResult.StringKey(className);
        Collection<BugInstance> bugs = instanceMap.get(key);

        if (null != bugs) {
            for (BugInstance bug : bugs) {
                try {
                    SourceLineAnnotation sourceLineAnnotation = null;
                    // Highest priority: return the first top level source line annotation
                    for (Iterator<BugAnnotation> annotationIterator = bug.annotationIterator(); annotationIterator.hasNext();) {
                        BugAnnotation annotation = annotationIterator.next();
                        if (annotation instanceof SourceLineAnnotation) {
                            sourceLineAnnotation = (SourceLineAnnotation) annotation;
                            break;
                        }
                    }
                    if ((null != sourceLineAnnotation) && (-1 != sourceLineAnnotation.getStartLine())) {
                        Line line = getLineForSourceAnnotation(DataObject.find(fileObject), sourceLineAnnotation);
                        annotate(bug, line, project);

                        continue;
                    }
                    FieldAnnotation fieldAnnotation = bug.getPrimaryField();
                    if (null != fieldAnnotation) {
                        VariableElementDescriptor desc = new VariableElementDescriptorImpl(bug.getPrimaryClass(), fieldAnnotation, project);
                        JavaElement findFieldElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findVariableElement(desc);
                        annotate(bug, findFieldElement, project);
                        continue;
                    }
                    MethodAnnotation methodAnnotation = bug.getPrimaryMethod();
                    if (null != methodAnnotation) {
                        MethodElementDescriptor desc = new MethodElementDescriptorImpl(bug.getPrimaryClass(), methodAnnotation, project);
                        JavaElement findMethodElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findMethodElement(desc);
                        annotate(bug, findMethodElement, project);
                        continue;
                    }
                    ClassAnnotation classAnnotation = bug.getPrimaryClass();
                    if (null != classAnnotation) {
                        ClassElementDescriptor desc = new ClassElementDescriptorImpl(classAnnotation, project);
                        JavaElement findClassElement = org.nbheaven.sqe.core.java.search.SearchUtilities.findClassElement(desc);
                        annotate(bug, findClassElement, project);
                        continue;
                    }
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
}
