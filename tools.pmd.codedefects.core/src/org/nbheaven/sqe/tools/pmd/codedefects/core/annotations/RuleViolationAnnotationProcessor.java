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
package org.nbheaven.sqe.tools.pmd.codedefects.core.annotations;

import java.util.Collection;
import net.sourceforge.pmd.RuleViolation;

import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.SQEAnnotationProcessor;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult;

import org.nbheaven.sqe.core.java.search.SearchUtilities;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;

import org.openide.ErrorManager;


import org.openide.filesystems.FileObject;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.Line.Set;

import java.util.Map;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDResult.ClassKey;
import org.openide.cookies.LineCookie;

/**
 *
 * @author Sven Reimers
 */
public final class RuleViolationAnnotationProcessor
        implements SQEAnnotationProcessor {

    public static final SQEAnnotationProcessor INSTANCE = new RuleViolationAnnotationProcessor();

    /**
     * Creates a new instance of RuleViolationAnnotationProcessor
     */
    private RuleViolationAnnotationProcessor() {
    }

    private static void openSourceFileAndAnnotate(RuleViolation ruleViolation,
            Line line, Project project) {
        annotate(ruleViolation, line, project);
        line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, ruleViolation.getBeginColumn());
    }

    public static Line getLineForRuleViolation(FileObject fo, RuleViolation ruleViolation) {
        try {
            DataObject dao = DataObject.find(fo);
            LineCookie cookie = dao.getCookie(LineCookie.class);
            Set lineset = cookie.getLineSet();
            int lineNum = ruleViolation.getBeginLine();

            return lineset.getOriginal(lineNum - 1);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
            throw new RuntimeException("Failure accesing DataObject fot FileObject " + fo);
        }
    }

    private static void annotate(final RuleViolation ruleViolation,
            final Line line, Project project) {
        PMDAnnotation annotation = PMDAnnotation.getNewInstance(project);
        annotation.setErrorMessage(ruleViolation.getDescription() + " [" + ruleViolation.getRule().getName() + "]");
        Part linePart = line.createPart(ruleViolation.getBeginColumn(), ruleViolation.getEndColumn());
        annotation.attach(linePart);
        line.addPropertyChangeListener(annotation);
    }

    public static void openSourceFile(RuleViolation ruleViolation,
            Project project) {
        // take care of default package
        String packagePrefix = ruleViolation.getPackageName().length() > 0 ? ruleViolation.getPackageName() + "." : "";
        String fileName = packagePrefix +
                (("".equals(ruleViolation.getFilename())) ? ruleViolation.getClassName()
                : ruleViolation.getFilename());
        FileObject fo = findFileObjectForAnnotatedClass(fileName);
        if (fo != null) {
            Line line = getLineForRuleViolation(fo, ruleViolation);
            openSourceFileAndAnnotate(ruleViolation, line, project);
        }
    }

    public static FileObject findFileObjectForAnnotatedClass(String className) {
        String javaFileName = className;

        if (-1 != javaFileName.indexOf('$')) {
            javaFileName = javaFileName.substring(0, javaFileName.indexOf('$'));
        }

        javaFileName = javaFileName.replaceAll("\\.", "/") + ".java";

        // com/ndsatcom/Schnulli.java
        return GlobalPathRegistry.getDefault().findResource(javaFileName);
    }

    @Override
    public void annotateSourceFile(final JavaSource javaSource,
            final Project project, final QualityResult qualityResult) {
        if (null == qualityResult) {
            return;
        }
        assert qualityResult instanceof PMDResult : "Illegal session passed to AnnotationProcessor";

        final PMDResult result = (PMDResult) qualityResult;

        FileObject fileObject = javaSource.getFileObjects().iterator().next();
        Collection<String> fqnClassNames = SearchUtilities.getFQNClassNames(fileObject);
        for (String fqnClassName : fqnClassNames) {
            annotateClass(fqnClassName, fileObject, project, result);
        }
    }

    @Override
    public void clearAllAnnotations(Project project) {
        PMDAnnotation.clearAll(project);
    }

    private void annotateClass(String className, FileObject fileObject, Project project, PMDResult result) {
        Map<Object, Collection<RuleViolation>> instanceMap = result.getInstanceByClass();
        ClassKey classKey = new ClassKey(className);
        Collection<RuleViolation> ruleViolations = instanceMap.get(classKey);
        if (null != ruleViolations) {
            for (RuleViolation ruleViolation : ruleViolations) {
                try {
                    Line line = getLineForRuleViolation(fileObject, ruleViolation);
                    annotate(ruleViolation, line, project);
                } catch (RuntimeException rex) {
                    ErrorManager.getDefault().notify(rex);
                }
            }
        }

    }
}
