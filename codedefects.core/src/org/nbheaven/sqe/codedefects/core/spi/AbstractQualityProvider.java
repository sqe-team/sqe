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

import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;

import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;

/**
 *
 * @author Sven Reimers
 */
public abstract class AbstractQualityProvider implements QualityProvider {

    /**
     * Creates a new instance of AbstractQualityProvider
     */
    protected AbstractQualityProvider() {
    }

    @Override
    public boolean isValidFor(Project project) {
        return SQECodedefectSupport.isQualityAwareProject(project, this)
                && ProjectUtilities.getJavaSourceGroups(project).length != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QualityProvider) {
            return getQualitySessionClass().equals(((QualityProvider) obj).getQualitySessionClass());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}
