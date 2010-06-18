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
package org.nbheaven.sqe.tools.findbugs.codedefects.hints;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.config.UserPreferences;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.core.java.search.ClassElementDescriptor;
import org.nbheaven.sqe.core.java.search.JavaElement;
import org.nbheaven.sqe.core.java.search.MethodElementDescriptor;
import org.nbheaven.sqe.core.java.search.SearchUtilities;
import org.nbheaven.sqe.core.java.search.VariableElementDescriptor;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsResult;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.ClassElementDescriptorImpl;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.MethodElementDescriptorImpl;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl.VariableElementDescriptorImpl;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.settings.FindBugsSettingsProvider;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * This is heavily inspired by the work done by Jan Lahoda - Big thank you!
 * @author Sven Reimers
 */
  public class FindBugsHint {

    private FindBugsHint() {}
    
    private static final RequestProcessor HINT_PROCESSOR = new RequestProcessor("FindBugs-Hint-Processor", 1);

    private static class Task extends FileChangeAdapter implements CancellableTask<CompilationInfo> {

        private final FileObject fileObject;
        private List<ErrorDescription> errors;

        @SuppressWarnings("LeakingThisInConstructor")
        private Task(FileObject fileObject) {
            this.fileObject = fileObject;
            fileObject.addFileChangeListener(FileUtil.weakFileChangeListener(this, fileObject));
        }

        public void cancel() {
            // TODO kill this
        }

        public void run(final CompilationInfo compilationInfo) throws Exception {
            final FileObject fo = compilationInfo.getFileObject();
            final Document document = compilationInfo.getDocument();

            if (null == errors && null != fo && null != document) {
                HINT_PROCESSOR.post(new Runnable() {

                    public void run() {
                        try {
                            errors = computeErrors(fo, document);
                            refresh(false);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                errors = Collections.emptyList();
            }
            if (null != errors && null != fo) {
                HintsController.setErrors(fo, Task.class.getName(), errors);
            }
        }

        private List<ErrorDescription> computeErrors(FileObject fileObject, Document document) throws Exception {
            Project project = FileOwnerQuery.getOwner(fileObject);
            if (null != project) {
                FindBugsSession session = project.getLookup().lookup(FindBugsSession.class);
                if (null != session) {
                    if (SQECodedefectProperties.isQualityProviderActive(project, session.getProvider())) {
                        List<ErrorDescription> computedErrors = new LinkedList<ErrorDescription>();
                        Map<FindBugsResult.ClassKey,Collection<BugInstance>> instanceByClass =
                                session.computeResultAndWait(fileObject).getInstanceByClass(true);
                        Collection<String> classes = SearchUtilities.getFQNClassNames(fileObject);
                        for (String className : classes) {
                            for (FindBugsResult.ClassKey classKey : instanceByClass.keySet()) {
                                if (classKey.getDisplayName().equals(className)) {
                                    Collection<BugInstance> bugs = instanceByClass.get(classKey);
                                    computedErrors.addAll(getErrors(project, bugs, fileObject, document));
                                }
                            }
                        }
                        return computedErrors;
                    }
                }
            }
            return Collections.emptyList();
        }

        private void refresh(boolean deepRefresh) {
            if (deepRefresh) {
                errors = null;
            }
            for (JavaSourceTaskFactory f : Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
                if (f instanceof Factory) {
                    ((Factory) f).refreshImpl(fileObject);
                }
            }
        }

        private List<ErrorDescription> getErrors(final Project project, Collection<BugInstance> bugs, final FileObject file, final Document document) {
            List<ErrorDescription> errorDescriptions = new LinkedList<ErrorDescription>();
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
                        List<Fix> fixes = new ArrayList<Fix>();
                        if (findElement != null) {
                            fixes.add(new SuppressWarningsFix(bugInstance.getType(), findElement.getHandle(), file));
                        }
                        fixes.add(new DisableDetectorFix(bugInstance, project));
                        errorDescriptions.add(ErrorDescriptionFactory.createErrorDescription(
                                    Severity.WARNING, "[FindBugs] " + bugInstance.getAbridgedMessage(),
                                    fixes, document, line));
                    }
                } catch (RuntimeException e) {
                    System.err.println("INFO: Can't create ErrorDescription for FindBugs bug instance: " +
                            bugInstance.getMessage());
                    e.printStackTrace();
                }
            }
            return errorDescriptions;
        }

        private JavaElement locateElement(BugInstance bugInstance, Project project) {
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

        public @Override void fileChanged(FileEvent fe) {
            // Just running refresh synchronously does not reliably pick up the new changes.
            // Nor does JavaSource.runWhenScanFinished. So just wait a second, as a hack.
            // Better to listen to CompileOnSaveHelper, once that is possible.
            HINT_PROCESSOR.post(new Runnable() {
                public void run() {
                    refresh(true);
                }
            }, 1000);
        }

        private static class DisableDetectorFix implements Fix {

            private final BugInstance bugInstance;
            private final Project project;

            public DisableDetectorFix(BugInstance bugInstance, Project project) {
                this.bugInstance = bugInstance;
                this.project = project;
            }

            public String getText() {
                return "Disable detector for \"" + bugInstance.getBugPattern().getShortDescription() + "\"";
            }

            public ChangeInfo implement() throws Exception {
                FindBugsSettingsProvider settingsProvider = project.getLookup().lookup(FindBugsSettingsProvider.class);
                if (settingsProvider != null) {
                    UserPreferences findBugsSettings = settingsProvider.getFindBugsSettings();
                    for (DetectorFactory detectorFactory : NbCollections.iterable(DetectorFactoryCollection.instance().factoryIterator())) {
                        if (detectorFactory.getReportedBugPatterns().contains(bugInstance.getBugPattern())) {
                            findBugsSettings.enableDetector(detectorFactory, false);
                        }
                    }
                    settingsProvider.setFindBugsSettings(findBugsSettings);
                    FindBugsSession qualitySession = project.getLookup().lookup(FindBugsSession.class);
                    FindBugsResult result = qualitySession.getResult();
                    if (result != null) {
                        result.removeAllBugInstancesForBugPattern(bugInstance.getBugPattern());
                    }
                }
                return null;
            }
        }

        private static class SuppressWarningsFix implements Fix { // SQE-8

            private final String bugType;
            private final ElementHandle<?> handle;
            private final FileObject file;

            SuppressWarningsFix(String bugType, ElementHandle<?> handle, FileObject file) {
                this.bugType = bugType;
                this.handle = handle;
                this.file = file;
            }

            public String getText() {
                return "Suppress warning";
            }

            public ChangeInfo implement() throws Exception {
                JavaSource.forFileObject(file).runModificationTask(new org.netbeans.api.java.source.Task<WorkingCopy>() {
                    public void run(WorkingCopy wc) throws Exception {
                        wc.toPhase(JavaSource.Phase.RESOLVED);
                        TypeElement sw = null;
                        for (ElementHandle<TypeElement> swh : wc.getClasspathInfo().getClassIndex().
                                getDeclaredTypes("SuppressWarnings", NameKind.SIMPLE_NAME, EnumSet.of(SearchScope.DEPENDENCIES, SearchScope.SOURCE))) {
                            TypeElement _sw = swh.resolve(wc);
                            if (_sw.getKind() != ElementKind.ANNOTATION_TYPE) {
                                continue;
                            }
                            Retention retention = _sw.getAnnotation(Retention.class);
                            if (retention != null && retention.value() == RetentionPolicy.SOURCE) {
                                continue;
                            }
                            // XXX look up @Target, make sure unspecified or matches element's kind
                            // XXX verify that it has a String[] value() attribute
                            sw = _sw;
                            break;
                        }
                        if (sw == null) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                    "<html>Could not find a <code>@SuppressWarnings</code> " +
                                    "with <code>@Retention(CLASS/RUNTIME)</code> in project classpath.<br>" +
                                    "Try <code>findbugs:annotations:*</code> for Maven, Common Annotations API for NetBeans modules, etc.",
                                    NotifyDescriptor.INFORMATION_MESSAGE));
                            // XXX try to add such a lib if it can be found somewhere
                            return;
                        }
                        TreeMaker make = wc.getTreeMaker();
                        Element element = handle.resolve(wc);
                        if (element == null) {
                            System.err.println("could not find " + handle);
                            return;
                        }
                        Tree elementTree = wc.getTrees().getTree(element);
                        ModifiersTree old;
                        if (elementTree.getKind() == Tree.Kind.CLASS) {
                            old = ((ClassTree) elementTree).getModifiers();
                        } else if (elementTree.getKind() == Tree.Kind.METHOD) {
                            old = ((MethodTree) elementTree).getModifiers();
                        } else if (elementTree.getKind() == Tree.Kind.VARIABLE) {
                            old = ((VariableTree) elementTree).getModifiers();
                        } else {
                            System.err.println("unknown tree kind " + elementTree.getKind());
                            return;
                        }
                        ModifiersTree nue = addSuppressWarnings(make, sw, old);
                        nue = GeneratorUtilities.get(wc).importFQNs(nue);
                        wc.rewrite(old, nue);
                    }
                }).commit();
                return null; // XXX would be polite to implement
            }

            private ModifiersTree addSuppressWarnings(TreeMaker make, TypeElement sw, ModifiersTree original) {
                LiteralTree toAdd = make.Literal(bugType);
                // First try to insert into a value list for an existing annotation:
                List<? extends AnnotationTree> anns = original.getAnnotations();
                for (int i = 0; i < anns.size(); i++) {
                    AnnotationTree ann = anns.get(i);
                    Tree annotationType = ann.getAnnotationType();
                    Kind kind = annotationType.getKind();
                    Name name;
                    switch (kind) {
                    case IDENTIFIER:
                        name = ((IdentifierTree) annotationType).getName();
                        break;
                    case MEMBER_SELECT:
                        name = ((MemberSelectTree) annotationType).getIdentifier();
                        break;
                    default:
                        System.err.println("got strange annotation type (" + kind + "): " + annotationType);
                        continue;
                    }
                    // XXX what if this is the java.lang version? how to distinguish??
                    if (name.contentEquals("SuppressWarnings")) {
                        List<? extends ExpressionTree> args = ann.getArguments();
                        // XXX need to rather skip over non-'value' assignments (e.g. 'justification')
                        if (args.size() != 1) {
                            System.err.println("args list for @SW not of size 1: " + args);
                            return original;
                        }
                        AssignmentTree assign = (AssignmentTree) args.get(0);
                        if (!assign.getVariable().toString().equals("value")) {
                            System.err.println("weird attribute for @SW: " + assign);
                            return original;
                        }
                        ExpressionTree arg = assign.getExpression();
                        NewArrayTree arr;
                        if (arg.getKind() == Tree.Kind.STRING_LITERAL) {
                            arr = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.singletonList(arg));
                        } else if (arg.getKind() == Tree.Kind.NEW_ARRAY) {
                            arr = (NewArrayTree) arg;
                        } else {
                            System.err.println("unknown arg kind " + arg.getKind() + ": " + arg);
                            return original;
                        }
                        for (ExpressionTree existing : arr.getInitializers()) {
                            if (((LiteralTree) existing).getValue().equals(bugType)) {
                                // Already suppressing this warning - perhaps have just not yet reanalyzed.
                                return original;
                            }
                        }
                        arr = make.addNewArrayInitializer(arr, toAdd);
                        ann = make.Annotation(annotationType, Collections.singletonList(arr));
                        return make.insertModifiersAnnotation(make.removeModifiersAnnotation(original, i), i, ann);
                    }
                }
                // Not found, so create a new annotation:
                ExpressionTree annotationTypeTree = make.QualIdent(sw);
                List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                arguments.add(/*make.Assignment(make.Identifier("value"), */toAdd/*)*/);
                AnnotationTree annTree = make.Annotation(annotationTypeTree, arguments);
                return make.addModifiersAnnotation(original, annTree);
            }

        }

    }

    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class Factory extends EditorAwareJavaSourceTaskFactory {

        public Factory() {
            super(Phase.UP_TO_DATE, Priority.MIN);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject fileObject) {
            return new Task(fileObject);
        }

        private void refreshImpl(FileObject file) {
            reschedule(file);
        }
    }
}
