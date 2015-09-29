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
package org.nbheaven.sqe.codedefects.core.api;

import org.netbeans.api.project.Project;

import java.beans.PropertyChangeListener;


/**
 *
 * @author Sven Reimers
 */
public interface QualitySession {
    public static final String RESULT = "Result";

    public QualityProvider getProvider();

    public Project getProject();

    public String getName();

    public String getDisplayName();

    public QualityResult getResult();

    public void computeResult();

    // PropertyChange
    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName,
        PropertyChangeListener listener);
}
