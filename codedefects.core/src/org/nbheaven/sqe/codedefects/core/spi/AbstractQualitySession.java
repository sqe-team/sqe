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
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;


/**
 *
 * @author Sven Reimers
 */
public abstract class AbstractQualitySession implements QualitySession {
    private final PropertyChangeSupport propSupport;
    private final Project project;
    private final QualityProvider provider;

    /** Creates a new instance of AbstractQualitySession */
    public AbstractQualitySession(final QualityProvider provider,
        final Project project) {
        this.provider = provider;
        this.project = project;
        this.propSupport = new PropertyChangeSupport(this);
        PropertyChangeListener listener = new AnnotationToggleListener(provider, project);
        SQECodedefectProperties.addPropertyChangeListener(SQECodedefectProperties.getPropertyNameAnnotateActive(provider), listener);        
        this.addPropertyChangeListener(QualitySession.RESULT, listener);        
    }

    public QualityProvider getProvider() {
        return provider;
    }

    public Project getProject() {
        return this.project;
    }

    public String getName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    public String getDisplayName() {
        return ProjectUtils.getInformation(project).getName();
    }

    protected final PropertyChangeSupport getPropertyChangeSupport() {
        return propSupport;
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    public final void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        getPropertyChangeSupport()
            .addPropertyChangeListener(propertyName, listener);
    }

    public final void removePropertyChangeListener(
        PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        getPropertyChangeSupport()
            .removePropertyChangeListener(propertyName, listener);
    }

    protected final void fireResultChanged(QualityResult oldResult,
        QualityResult newResult) {
        getPropertyChangeSupport()
            .firePropertyChange(QualitySession.RESULT, oldResult, newResult);
    }
}
