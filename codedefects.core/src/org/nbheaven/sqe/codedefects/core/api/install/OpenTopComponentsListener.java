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
package org.nbheaven.sqe.codedefects.core.api.install;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.api.SQEAnnotationProcessor;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Sven Reimers
 */
final class OpenTopComponentsListener implements PropertyChangeListener {

    static PropertyChangeListener INSTANCE = new OpenTopComponentsListener();

    private static final RequestProcessor RP = new RequestProcessor(OpenTopComponentsListener.class.getName());

    @Override
    public void propertyChange(final PropertyChangeEvent event) {

        // The list of open projects has changed
        if (TopComponent.Registry.PROP_OPENED.equals(
                event.getPropertyName())) {
            @SuppressWarnings("unchecked")
            Set<TopComponent> oldComponents = (Set<TopComponent>) event.getOldValue();
            @SuppressWarnings("unchecked")
            Set<TopComponent> newComponents = (Set<TopComponent>) event.getNewValue();
            newComponents.removeAll(oldComponents);

            for (TopComponent tc : newComponents) {
                final DataObject dao = tc.getLookup().lookup(DataObject.class);

                if (null != dao) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                    for (final Project project : OpenProjects.getDefault().getOpenProjects()) {
                            SourceGroup[] sgs = ProjectUtilities.getJavaSourceGroups(project);

                            for (SourceGroup sg : sgs) {
                                try {
                                    if (sg.contains(dao.getPrimaryFile())) {
                                        final JavaSource javaSource = JavaSource.forFileObject(dao.getPrimaryFile());
                                        if (null != javaSource) {
                                            for (final QualitySession qualitySession : project.getLookup().lookupAll(QualitySession.class)) {
                                                assert qualitySession != null : "Illegal null QualitySession in QualitySessionManager-Storage for Project " +
                                                        project;

                                                final SQEAnnotationProcessor sqeAnnotationProcessor =
                                                        qualitySession.getProvider().getLookup().lookup(SQEAnnotationProcessor.class);
                                                if (null == sqeAnnotationProcessor) {
                                                    continue;
                                                }
                                                if (SQECodedefectProperties.isQualityProviderAnnotateActive(project, qualitySession.getProvider())) {
                                                            sqeAnnotationProcessor.annotateSourceFile(javaSource,
                                                                    project,
                                                                    qualitySession.getResult());
                                                }
                                            }
                                        }

                                        break;
                                    }
                                } catch (IllegalArgumentException iaex) {
                                    // Hmm, not sure what to do about it right now, but seems to work
                                    //ErrorManager.getDefault().notify(iaex);
                                    }
                            }
                    }
                        }
                    });
                }
            }
        }
    }
}