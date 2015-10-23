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
package org.nbheaven.sqe.tools.pmd.codedefects.hints;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import net.sourceforge.pmd.RuleViolation;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDSession;
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
 * @author Florian Vogler
 */
final class PMDHintTask implements CancellableTask<CompilationInfo> {

    private static final RequestProcessor HINT_PROCESSOR = new RequestProcessor("PMD-Hint-Processor", 1);
    private List<ErrorDescription> errors;

    public PMDHintTask() {
    }

    @Override
    public void cancel() {
        // TODO kill this
    }

    @Override
    public synchronized void run(final CompilationInfo compilationInfo) throws Exception {
        final FileObject fileObject = compilationInfo.getFileObject();
        if (null != fileObject) {
            if (SQECodedefectSupport.isQualityProviderEnabledForFileObject(fileObject, PMDSession.class)) {
                if (null == errors) {
                    System.out.println("PMDHintTask: (calc) " + System.identityHashCode(fileObject));
                    final Document document = compilationInfo.getDocument();
                    if (null != document) {
                        HINT_PROCESSOR.post(() -> {
                            try {
                                errors = computeErrors(fileObject, document);
                                PMDHintTaskFactory.rescheduleFile(fileObject);
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        });
                    } else {
                        errors = Collections.emptyList();
                    }
                } else {
                    System.out.println("PMDHintTask: (show) " + System.identityHashCode(fileObject));
                    HintsController.setErrors(fileObject, getClass().getName(), errors);
                    errors = null;
                }
            } else {
                errors = null;
                HintsController.setErrors(fileObject, getClass().getName(), Collections.emptyList());
            }
        }
    }

    private static List<ErrorDescription> computeErrors(FileObject fileObject, Document document) throws Exception {
        PMDSession session = SQECodedefectSupport.retrieveSessionFromFileObject(fileObject, PMDSession.class);
        PMDResult result = PMDSession.computeResultAndWait(fileObject);
        if (result != null) {
            List<ErrorDescription> computedErrors = new LinkedList<>();
            Project project = FileOwnerQuery.getOwner(fileObject);

            // XXX see comment in ClassKey constructor
            Map<PMDResult.ClassKey, Collection<RuleViolation>> instanceByClass = result.getInstanceByClass();
            instanceByClass.keySet().stream()
                    .filter((classKey) -> (classKey.getDisplayName().equals(fileObject.getPath())))
                    .map((classKey) -> instanceByClass.get(classKey))
                    .forEach((bugs) -> {
                        computedErrors.addAll(createErrorDescription(project, fileObject, document, bugs));
                    });

            return computedErrors;
        }
        return Collections.emptyList();
    }

    private static List<ErrorDescription> createErrorDescription(Project project, FileObject fileObject, Document document, Collection<RuleViolation> ruleViolations) {
        List<ErrorDescription> errorDescriptions = new LinkedList<>();
        ruleViolations.stream().forEach((ruleViolation) -> {
            Fix fix = new DisablePMDRuleFix(ruleViolation, project);
            try {
                ErrorDescription error = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "[PMD] " + ruleViolation.getDescription(), Arrays.asList(new Fix[]{fix}), document, Math.max(1, ruleViolation.getBeginLine()));
                errorDescriptions.add(error);
            } catch (RuntimeException e) {
                Logger.getLogger(PMDHintTask.class.getName()).log(Level.INFO,
                        "Can't create ErrorDescription for pmd rule violation: {0}[{1}:{2}]",
                        new Object[]{ruleViolation.getDescription(), ruleViolation.getClassName(), ruleViolation.getBeginLine()});
            }
        });
        return errorDescriptions;
    }

}
