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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.stream.Collectors;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.api.SQEAnnotationProcessor;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Sven Reimers
 */
class AnnotationToggleListener implements PropertyChangeListener {

    private static final RequestProcessor ANNOTATOR_QUEUE = new RequestProcessor("SQECodedefectFileAnnotator", 3);

    private final QualityProvider qualityProvider;
    private final Project project;

    AnnotationToggleListener(QualityProvider qualityProvider, Project project) {
        this.qualityProvider = qualityProvider;
        this.project = project;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if (SQECodedefectProperties.getPropertyNameAnnotateActive(qualityProvider).equals(propertyName)) {
            if (SQECodedefectProperties.isQualityProviderAnnotateActive(project, qualityProvider)) {
                annotateAllSourceFiles();
            } else {
                clearAllAnnotations();
            }

        } else if (QualitySession.RESULT.equals(propertyName)) {
            // Reset annotations
            clearAllAnnotations();
            annotateAllSourceFiles();
        }
    }

    private void clearAllAnnotations() {
        SQEAnnotationProcessor processor = qualityProvider.getLookup().lookup(SQEAnnotationProcessor.class);
        processor.clearAllAnnotations(project);
    }

    private void annotateAllSourceFiles() {
        final QualitySession session = SQECodedefectSupport.retrieveSession(project, qualityProvider);
        final SQEAnnotationProcessor processor = qualityProvider.getLookup().lookup(SQEAnnotationProcessor.class);

        Set<JavaSource> javaSources = TopComponent.getRegistry().getOpened().stream()
                .map(topComponent -> topComponent.getLookup().lookup(DataObject.class))
                .filter(dao -> null != dao)
                .map(dao -> JavaSource.forFileObject(dao.getPrimaryFile()))
                .filter(javaSource -> null != javaSource)
                .collect(Collectors.toSet());

        javaSources.forEach((javaSource) -> {
            ANNOTATOR_QUEUE.execute(() -> {
                System.out.println("annotateSourceFile - processor: " + processor.getClass().getName() + " - " + javaSource + "#" + javaSource.getFileObjects().toArray());
                processor.annotateSourceFile(javaSource, project, session.getResult());
            });
        });
    }
}
