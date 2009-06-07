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
import java.util.HashSet;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

/**
 *
 * @author Sven Reimers
 */
final public class QualitySessionSupport {

    public static Collection<Project> getQualityEnhancedProjects() {
        Collection<Project> projects = new HashSet<Project>();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            if (null != project.getLookup().lookup(QualitySession.class)) {
                projects.add(project);
            }
        }
        return projects;
    }
}
