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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
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
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
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
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * This is heavily inspired by the work done ny Jan Lahoda - Big thank you!
 * @author Sven Reimers
 */
public class FindBugsHint {

    private static RequestProcessor HINT_PROCESSOR = new RequestProcessor("FindBugs-Hint-Processor", 1);

    private static class Task implements CancellableTask<CompilationInfo> {

        private FileObject fileObject;
        private List<ErrorDescription> errors;
        private FileChangeListener listener = null;

        private Task(FileObject fileObject) {
            this.fileObject = fileObject;
            this.listener = new FCL(this);
            register();
        }

        private void register() {
            ClassPath sourceCP = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
            if (sourceCP != null) {
                FileObject root = sourceCP.findOwnerRoot(fileObject);
                try {
                    String base = sourceCP.getResourceName(fileObject, File.separatorChar, false);
                    String name =  base + ".class"; //XXX
                    Result bin = BinaryForSourceQuery.findBinaryRoots(root.getURL());
                    for (URL u : bin.getRoots()) {
                        if ("file".equals(u.getProtocol())) {
                            try {
                                File cls = new File(new File(u.toURI()), name);
                                if (cls.exists()) {
                                    FileChangeListener clsWeakFileChangeListener = FileUtil.weakFileChangeListener(listener, cls);
                                    FileUtil.addFileChangeListener(clsWeakFileChangeListener, cls);
                                }
                            } catch (URISyntaxException x) {
                                Exceptions.printStackTrace(x);
                            }
                        }
                    }
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
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
                        Map<FindBugsResult.ClassKey, Collection<BugInstance>> instanceByClass = session.computeResultAndWait(getFileObjectsToScan()).getInstanceByClass(true);
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

        private FileObject[] getFileObjectsToScan() {
            Collection<FileObject> fileObjectCollection = new LinkedList<FileObject>();
            ClassPath sourceCP = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
            if (sourceCP != null) {
                FileObject root = sourceCP.findOwnerRoot(fileObject);
                try {
                    String base = sourceCP.getResourceName(fileObject, File.separatorChar, false);
                    String name =  base + ".class"; //XXX
                    int lastSlashIndex = base.lastIndexOf(File.separatorChar);
                    String className = base.substring(lastSlashIndex > 0 ? lastSlashIndex + 1 : 0);
                    Result bin = BinaryForSourceQuery.findBinaryRoots(root.getURL());
                    for (URL u : bin.getRoots()) {
                        if ("file".equals(u.getProtocol())) {
                            try {
                                File cls = new File(new File(u.toURI()), name);
                                if (cls.exists()) {
                                    for(FileObject child: FileUtil.toFileObject(cls.getParentFile()).getChildren()) {
                                        if(!child.isFolder() && child.getName().startsWith(className) && /*SQE-13*/child.hasExt("class")) {
                                            fileObjectCollection.add(child);
                                        }
                                    }
                                }
                            } catch (URISyntaxException x) {
                                Exceptions.printStackTrace(x);
                            }
                        }
                    }
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return fileObjectCollection.toArray(new FileObject[fileObjectCollection.size()]);
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
                                    "Could not find a @SuppressWarnings with @Retention(CLASS/RUNTIME) in project classpath. " +
                                    "Try findbugs:annotations:* for Maven, Common Annotations API for NetBeans modules, etc.",
                                    NotifyDescriptor.WARNING_MESSAGE));
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
                            // XXX what about constructors?
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
                // XXX check if already exists on old, in which case insert into array
                ExpressionTree annotationTypeTree = make.QualIdent(sw);
                List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                arguments.add(/*make.Assignment(make.Identifier("value"), */make.Literal(bugType)/*)*/);
                AnnotationTree annTree = make.Annotation(annotationTypeTree, arguments);
                return make.addModifiersAnnotation(original, annTree);
            }

        }

    }

    private static final class FCL implements FileChangeListener {

        private Task task;

        private FCL(Task task) {
            this.task = task;
        }

        public void fileFolderCreated(FileEvent fe) {
            task.refresh(true);
        }

        public void fileDataCreated(FileEvent fe) {
            task.refresh(true);
        }

        public void fileChanged(FileEvent fe) {
            task.refresh(true);
        }

        public void fileDeleted(FileEvent fe) {
            task.refresh(true);
        }

        public void fileRenamed(FileRenameEvent fe) {
            task.refresh(true);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            task.refresh(true);
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

        protected void refreshImpl(FileObject file) {
            reschedule(file);
        }
    }
}
