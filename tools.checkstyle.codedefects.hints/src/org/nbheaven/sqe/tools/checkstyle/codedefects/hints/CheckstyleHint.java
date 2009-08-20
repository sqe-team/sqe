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
package org.nbheaven.sqe.tools.checkstyle.codedefects.hints;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.core.java.search.SearchUtilities;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleSession;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * This is heavily inspired by the work done ny Jan Lahoda - Big thank you!
 * @author Sven Reimers
 */
public class CheckstyleHint {

    private static RequestProcessor HINT_PROCESSOR = new RequestProcessor("Checkstyle-Hint-Processor", 1);

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
//            ClassPath sourceCP = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
//            if (sourceCP != null) {
//                FileObject root = sourceCP.findOwnerRoot(fileObject);
//                try {
//                    String name = sourceCP.getResourceName(fileObject, File.separatorChar, false) + ".class"; //XXX
//                    Result bin = BinaryForSourceQuery.findBinaryRoots(root.getURL());
//                    for (URL u : bin.getRoots()) {
//                        if ("file".equals(u.getProtocol())) {
            FileChangeListener weakFileChangeListener = FileUtil.weakFileChangeListener(listener, fileObject);
            if (fileObject.isData()) {
                fileObject.addFileChangeListener(weakFileChangeListener);
            }
//                        }
//                    }
//                } catch (FileStateInvalidException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
        }

        public void cancel() {
            // TODO kill this
        }

        public void run(final CompilationInfo compilationInfo) throws Exception {
            final FileObject fileObject = compilationInfo.getFileObject();
            final Document document = compilationInfo.getDocument();

            if (null == errors && null != fileObject && null != document) {
                HINT_PROCESSOR.post(new Runnable() {

                    public void run() {
                        try {
                            errors = computeErrors(fileObject, document);
                            refresh(false);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                errors = Collections.emptyList();
            }
            if (null != errors && null != fileObject) {
                HintsController.setErrors(fileObject, Task.class.getName(), errors);
            }
        }

        private List<ErrorDescription> computeErrors(FileObject fileObject, Document document) throws Exception {
            Project project = FileOwnerQuery.getOwner(fileObject);
            if (null != project) {
                CheckstyleSession session = project.getLookup().lookup(CheckstyleSession.class);
                if (null != session) {
                    if (SQECodedefectProperties.isQualityProviderActive(project, session.getProvider())) {
                        List<ErrorDescription> computedErrors = new LinkedList<ErrorDescription>();
                        Map<Object, Collection<AuditEvent>> instanceByClass = session.computeResultAndWait(fileObject).getInstanceByClass();
                        Collection<String> classes = SearchUtilities.getFQNClassNames(fileObject);
                        for (Object key : instanceByClass.keySet()) {
                            CheckstyleResult.ClassKey classKey = (CheckstyleResult.ClassKey) key;
                            if (classKey.getDisplayName().equals(fileObject.getPath())) {
                                Collection<AuditEvent> bugs = instanceByClass.get(classKey);
                                computedErrors.addAll(getErrors(project, bugs, fileObject, document));
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

        private List<ErrorDescription> getErrors(final Project project, Collection<AuditEvent> auditEvents, final FileObject file, final Document document) {
            List<ErrorDescription> errorDescriptions = new LinkedList<ErrorDescription>();
            for (final AuditEvent auditEvent : auditEvents) {

//                Fix fix = new Fix() {
//
//                    public String getText() {
//                        return "Disable Checkstyle Detector for BugPattern: " + auditEvent.getMessage();
//                    }
//
//                    public ChangeInfo implement() throws Exception {
//                        CheckstyleSettingsProvider settingsProvider = project.getLookup().lookup(CheckstyleSettingsProvider.class);
//                        if (null != settingsProvider) {
//                            CheckstyleSettings settings = settingsProvider.getCheckstyleSettings();
//
//                            for (Iterator<DetectorFactory> factoryIterator = DetectorFactoryCollection.instance().factoryIterator(); factoryIterator.hasNext();) {
//                                DetectorFactory detectorFactory = factoryIterator.next();
//                                if (detectorFactory.getReportedBugPatterns().contains(auditEvent.getBugPattern())) {
//                                    settings.enableDetector(detectorFactory, false);
//                                }
//                            }
//                            settingsProvider.setFindBugsSettings(settings);
//                        }
//                        return new ChangeInfo();
//                    }
//                };
                try {
                    ErrorDescription error = ErrorDescriptionFactory.createErrorDescription(
                            Severity.WARNING, "[Checkstyle] " + auditEvent.getMessage(), /*Arrays.asList(new Fix[]{fix}),*/
                            document, Math.max(1, auditEvent.getLine()));
                    errorDescriptions.add(error);
                } catch (RuntimeException e) {
                    System.err.println("INFO: Can't create ErrorDescription for checkstyle audit event: " +
                            auditEvent.getMessage() + "[" + auditEvent.getSourceName() + ":" + auditEvent.getLine() + "]");
                    e.printStackTrace();
                }
            }
            return errorDescriptions;
        }
    }

    private static final class FCL implements FileChangeListener {

        private Task task;

        private FCL(Task task) {
            this.task = task;
        }

        public void fileFolderCreated(FileEvent fe) {
        }

        public void fileDataCreated(FileEvent fe) {
            task.refresh(true);
        }

        public void fileChanged(FileEvent fe) {
            task.refresh(true);
        }

        public void fileDeleted(FileEvent fe) {
        }

        public void fileRenamed(FileRenameEvent fe) {
            task.refresh(true);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }

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
