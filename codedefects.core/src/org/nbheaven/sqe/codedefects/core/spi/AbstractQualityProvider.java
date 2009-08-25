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

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

/**
 *
 * @author sven
 */
public abstract class AbstractQualityProvider implements QualityProvider {

    /** Creates a new instance of AbstractQualityProvider */
    protected AbstractQualityProvider() {
    }

    public boolean isValidFor(Project project) {
        if (null == project) {
            return false;
        }

        // disable quality provider if the project has no registered Session
        if (null == project.getLookup().lookup(this.getQualitySessionClass())) {
            return false;
        }

        Sources sources = project.getLookup().lookup(Sources.class);

        if (null == sources) {
            return false;
        }

        SourceGroup[] sourceGroups = sources.getSourceGroups("java");

        if (sourceGroups.length == 0) {
            return false;
        }

        return true;
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
