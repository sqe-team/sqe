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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.annotations;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import java.util.Collection;
import java.util.Map;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.SQEAnnotationProcessor;
import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult.ClassKey;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.Line.Set;

/**
 *
 * @author Sven Reimers
 */
public final class AuditEventAnnotationProcessor implements SQEAnnotationProcessor {

    public static final SQEAnnotationProcessor INSTANCE = new AuditEventAnnotationProcessor();

    /**
     * Creates a new instance of AuditEventAnnotationProcessor
     */
    private AuditEventAnnotationProcessor() {
    }

    private static void openSourceFileAndAnnotate(AuditEvent auditEvent,
            Line line, Project project) {
        annotate(auditEvent, line, project);
        line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, auditEvent.getColumn() - 1);
    }

    public static Line getLineForRuleViolation(FileObject fo, AuditEvent auditEvent) {
        try {
            DataObject dao = DataObject.find(fo);
            LineCookie cookie = dao.getLookup().lookup(LineCookie.class);
            Set lineset = cookie.getLineSet();
            int lineNum = auditEvent.getLine();

            return lineset.getOriginal((lineNum > 0) ? (lineNum - 1) : 0);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
            throw new RuntimeException("Failure accesing DataObject fot FileObject " + fo);
        }
    }

    private static void annotate(final AuditEvent auditEvent, final Line line,
            Project project) {
        CheckstyleAnnotation annotation = CheckstyleAnnotation.getNewInstance(project);
        annotation.setErrorMessage(auditEvent.getLocalizedMessage().getMessage());
        annotation.attach(line);
        line.addPropertyChangeListener(annotation);
    }

    public static FileObject getFileObjectForAuditEvent(AuditEvent auditEvent, Project project) {
        String fullFileName = auditEvent.getFileName();

        SourceGroup[] groups = ProjectUtilities.getJavaSourceGroups(project);

        for (SourceGroup g : groups) {
            FileObject rootOfSourceFolder = g.getRootFolder();
            String rootFolderAsString = FileUtil.toFile(rootOfSourceFolder).getAbsolutePath();

            if (fullFileName.startsWith(rootFolderAsString)) {
                fullFileName = fullFileName.substring(rootFolderAsString.length() + 1);
                break;
            }
        }

        if (fullFileName.indexOf('$') != -1) {
            fullFileName = fullFileName.substring(0, fullFileName.indexOf('$'));
        }

        // com/ndsatcom/Schnulli.java
        return GlobalPathRegistry.getDefault().findResource(fullFileName);
    }

    public static void openSourceFile(AuditEvent auditEvent, Project project) {
        FileObject fo = getFileObjectForAuditEvent(auditEvent, project);

        if (null != fo) {
            Line line = getLineForRuleViolation(fo, auditEvent);
            openSourceFileAndAnnotate(auditEvent, line, project);
        }
    }

    public void annotateSourceFile(JavaSource javaSource,
            final Project project, QualityResult qualityResult) {
        if (null == qualityResult) {
            return;
        }
        assert qualityResult instanceof CheckstyleResult : "Illegal session passed to AnnotationProcessor";

        final CheckstyleResult result = (CheckstyleResult) qualityResult;

        for (FileObject fo : javaSource.getFileObjects()) {
            Map<Object, Collection<AuditEvent>> instanceMap = result.getInstanceByClass();
            ClassKey classKey = new ClassKey(fo);
            Collection<AuditEvent> auditEvents = instanceMap.get(classKey);

            if (null != auditEvents) {
                for (AuditEvent auditEvent : auditEvents) {
                    try {
                        Line line = getLineForRuleViolation(fo,
                                auditEvent);
                        annotate(auditEvent, line, project);
                    } catch (RuntimeException rex) {
                        ErrorManager.getDefault().notify(rex);
                    }
                }
            }
        }
    }

    public void clearAllAnnotations(Project project) {
        CheckstyleAnnotation.clearAll(project);
    }
}
