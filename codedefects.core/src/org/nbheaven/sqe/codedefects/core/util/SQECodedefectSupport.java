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
package org.nbheaven.sqe.codedefects.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Sven Reimers
 */
public final class SQECodedefectSupport {

    public static Collection<Project> getQualityEnhancedProjects() {
        Collection<Project> projects = new HashSet<>();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            if (isQualityAwareProject(project)) {
                projects.add(project);
            }
        }
        return projects;
    }

    public static final boolean isQualityAwareProject(Project project) {
        return null != retrieveSession(project, QualitySession.class);
    }

    public static final boolean isQualityAwareProject(Project project, QualityProvider provider) {
        return null != retrieveSession(project, provider);
    }

    public static QualitySession retrieveSession(Project project, QualityProvider provider) {
        return provider != null ? retrieveSession(project, provider.getQualitySessionClass()) : null;
    }

    public static <T extends QualitySession> T retrieveSession(FileObject fileObject, Class<T> sessionClass) {
        Project project = SQEProjectSupport.findProject(fileObject);
        return project != null ? retrieveSession(project, sessionClass) : null;
    }

    public static <T extends QualitySession> T retrieveSession(Project project, Class<T> sessionClass) {
        return (project != null && sessionClass != null) ? project.getLookup().lookup(sessionClass) : null;
    }

    public static Collection<? extends QualitySession> retrieveSessions(Project project) {
        return project != null ? project.getLookup().lookupAll(QualitySession.class) : Collections.emptyList();
    }

    public static boolean isQualityProviderActive(FileObject fileObject, QualityProvider provider) {
        return provider != null && isQualityProviderActive(fileObject, provider.getQualitySessionClass());
    }

    public static boolean isQualityProviderActive(Project project, QualityProvider provider) {
        return provider != null && isQualityProviderActive(project, provider.getQualitySessionClass());
    }

    public static boolean isQualityProviderActive(FileObject fileObject, Class<? extends QualitySession> sessionClass) {
        Project project = SQEProjectSupport.findProject(fileObject);
        return project != null && isQualityProviderActive(project, sessionClass);
    }

    public static boolean isQualityProviderActive(Project project, Class<? extends QualitySession> sessionClass) {
        return project != null && sessionClass != null && isQualityProviderActive(project, retrieveSession(project, sessionClass));
    }

    public static boolean isQualityProviderActive(Project project, QualitySession session) {
        return null != project && null != session && SQECodedefectProperties.isQualityProviderActive(project, session.getProvider());
    }
}
