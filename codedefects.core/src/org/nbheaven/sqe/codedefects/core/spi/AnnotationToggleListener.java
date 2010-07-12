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
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.api.SQEAnnotationProcessor;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Sven Reimers
 */
class AnnotationToggleListener implements PropertyChangeListener {

    private QualityProvider qualityProvider;
    private Project project;

    AnnotationToggleListener(QualityProvider qualityProvider, Project project) {
        this.qualityProvider = qualityProvider;
        this.project = project;
    }
        
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (propertyName != null && !propertyName.equals(SQECodedefectProperties.getPropertyNameAnnotateActive(qualityProvider)) && !propertyName.equals(QualitySession.RESULT)) {
            // #188604: cannot use addPropertyChangeListener(String,PropertyChangeListener) in conjunction with WeakListeners
            return;
        }
        if (!SQECodedefectProperties.isQualityProviderAnnotateActive(project, qualityProvider)) {
            SQEAnnotationProcessor processor = qualityProvider.getLookup().lookup(SQEAnnotationProcessor.class);
            processor.clearAllAnnotations(project);
        }  else {
            final SQEAnnotationProcessor processor = qualityProvider.getLookup().lookup(SQEAnnotationProcessor.class);
            if (propertyName.equals(QualitySession.RESULT)) {
                // reset annotations
                processor.clearAllAnnotations(project);
            }
            final QualitySession session = project.getLookup().lookup(qualityProvider.getQualitySessionClass());
            for (TopComponent topComponent: TopComponent.getRegistry().getOpened()) {
                DataObject dao = topComponent.getLookup().lookup(DataObject.class);
                if (null != dao) {
                    FileObject fo = dao.getPrimaryFile();
                    final JavaSource javaSource = JavaSource.forFileObject(fo);
                    if (null != javaSource) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                processor.annotateSourceFile(javaSource, project, session.getResult());
                            }
                        });
                    }
                }

            }
        }
    }
}
