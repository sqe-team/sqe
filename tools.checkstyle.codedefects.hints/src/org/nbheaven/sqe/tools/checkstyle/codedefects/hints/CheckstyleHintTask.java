/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbheaven.sqe.tools.checkstyle.codedefects.hints;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleSession;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author fvo
 */
final class CheckstyleHintTask implements CancellableTask<CompilationInfo> {

    private static final RequestProcessor HINT_PROCESSOR = new RequestProcessor("Checkstyle-Hint-Processor", 1);

    private List<ErrorDescription> errors;

    public CheckstyleHintTask() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public synchronized void run(final CompilationInfo compilationInfo) throws Exception {
        final FileObject fileObject = compilationInfo.getFileObject();
        if (null != fileObject) {
            if (null == errors) {
                System.out.println("CheckstyleHintTask: (calc) " + System.identityHashCode(fileObject));
                final Document document = compilationInfo.getDocument();
                if (null != document) {
                    HINT_PROCESSOR.post(() -> {
                        try {
                            errors = computeErrors(fileObject, document);
                            CheckstyleHintTaskFactory.rescheduleFile(fileObject);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    });
                } else {
                    errors = Collections.emptyList();
                }
            } else {
                System.out.println("CheckstyleHintTask: (show) " + System.identityHashCode(fileObject));
                HintsController.setErrors(fileObject, getClass().getName(), errors);
                errors = null;
            }
        }
    }

    private static List<ErrorDescription> computeErrors(FileObject fileObject, Document document) throws Exception {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (null != project) {
            CheckstyleSession session = project.getLookup().lookup(CheckstyleSession.class);
            if (null != session) {
                if (SQECodedefectProperties.isQualityProviderActive(project, session.getProvider())) {
                    List<ErrorDescription> computedErrors = new LinkedList<>();
                    CheckstyleResult result = session.computeResultAndWait(fileObject);
                    if (result != null) {
                        // XXX see comment in ClassKey constructor
                        Map<CheckstyleResult.ClassKey, Collection<AuditEvent>> instanceByClass = result.getInstanceByClass();
                        instanceByClass.keySet().stream()
                                .filter((classKey) -> (classKey.getDisplayName().equals(fileObject.getPath())))
                                .map((classKey) -> instanceByClass.get(classKey))
                                .forEach((bugs) -> {
                                    computedErrors.addAll(getErrors(project, bugs, fileObject, document));
                                });
                    }
                    return computedErrors;
                }
            }
        }
        return Collections.emptyList();
    }

    private static List<ErrorDescription> getErrors(final Project project, Collection<AuditEvent> auditEvents, final FileObject file, final Document document) {
        List<ErrorDescription> errorDescriptions = new LinkedList<>();
        auditEvents.stream().forEach((auditEvent) -> {
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
                ErrorDescription error = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "[Checkstyle] " + auditEvent.getMessage(), /*Arrays.asList(new Fix[]{fix}),*/ document, Math.max(1, auditEvent.getLine()));
                errorDescriptions.add(error);
            } catch (RuntimeException e) {
                Logger.getLogger(CheckstyleHintTask.class.getName()).log(Level.INFO,
                        "INFO: Can''t create ErrorDescription for checkstyle audit event: {0}[{1}:{2}]",
                        new Object[]{auditEvent.getMessage(), auditEvent.getSourceName(), auditEvent.getLine()});
            }
        });
        return errorDescriptions;
    }

}
