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
package org.nbheaven.sqe.codedefects.core.spi;

import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.value.ObservableValue;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.api.SQEAnnotationProcessor;
import org.netbeans.api.java.source.JavaSource;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Sven Reimers
 */
final class AnnotationControler {

    private static final RequestProcessor PROCESSOR = new RequestProcessor("SQECodedefectFileAnnotator", 3);
    private final QualitySession session;

    public AnnotationControler(QualitySession session) {
        this.session = session;
    }

    public void bind() {
        session.getAnnotateProjectResultEffectiveEnabledProperty().addListener(this::annotateProjectResultEnabled);
        session.getResultProperty().addListener(this::resultChanged);
        if (session.isAnnotateProjectResultEffectiveEnabled()) {
            annotateAllSourceFiles();
        } else {
            clearAllAnnotations();
        }
    }

    public void unbind() {
        session.getAnnotateProjectResultEffectiveEnabledProperty().removeListener(this::annotateProjectResultEnabled);
        session.getResultProperty().removeListener(this::resultChanged);
        clearAllAnnotations();
    }

    private void annotateProjectResultEnabled(ObservableValue<? extends Boolean> observable, Boolean oldEnabled, Boolean newEnabled) {
        if (newEnabled) {
            annotateAllSourceFiles();
        } else {
            clearAllAnnotations();
        }
    }

    private void resultChanged(ObservableValue<? extends QualityResult> observable, QualityResult oldValue, QualityResult newValue) {
        // Reset annotations
        clearAllAnnotations();
        annotateAllSourceFiles();
    }

    private SQEAnnotationProcessor getAnnotationProcessor() {
        return session.getProvider().getLookup().lookup(SQEAnnotationProcessor.class);
    }

    private void clearAllAnnotations() {
        getAnnotationProcessor().clearAllAnnotations(session.getProject());
    }

    private void annotateAllSourceFiles() {
        Set<JavaSource> javaSources = TopComponent.getRegistry().getOpened().stream()
                .map(topComponent -> topComponent.getLookup().lookup(DataObject.class))
                .filter(dao -> null != dao)
                .map(dao -> JavaSource.forFileObject(dao.getPrimaryFile()))
                .filter(javaSource -> null != javaSource)
                .collect(Collectors.toSet());

        javaSources.forEach((javaSource) -> {
            PROCESSOR.execute(() -> {
                SQEAnnotationProcessor annotationProcessor = getAnnotationProcessor();
                System.out.println("annotateSourceFile - processor: " + annotationProcessor.getClass().getName() + " - " + javaSource + "#" + javaSource.getFileObjects().toArray());
                annotationProcessor.annotateSourceFile(javaSource, session.getProject(), session.getResult());
            });
        });
    }
}
