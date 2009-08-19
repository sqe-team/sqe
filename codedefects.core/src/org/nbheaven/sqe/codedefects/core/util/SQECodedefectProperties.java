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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.core.ant.AntUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.NbPreferences;


/**
 *
 * @author Sven Reimers
 */
public final class SQECodedefectProperties {

    private static final String QUALITY_PROVIDER_PREFIX_ACTIVE = "run.provider.";
    private static final String QUALITY_PROVIDER_PREFIX_ANNOTATION_ACTIVE = "annotation.provider.";

    private static PropertyChangeSupport changeSupport = new PropertyChangeSupport(SQECodedefectProperties.class);
    
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
    public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }
    
    private static void firePropertyChange(String property, Object oldValule, Object newValue) {
        changeSupport.firePropertyChange(property, oldValule, newValue);
    }
    
    public static String getPropertyNameActive(QualityProvider qualityProvider) {
        return QUALITY_PROVIDER_PREFIX_ACTIVE + qualityProvider.getId().toLowerCase();
    }

    public static String getPropertyNameAnnotateActive(QualityProvider qualityProvider) {
        return QUALITY_PROVIDER_PREFIX_ANNOTATION_ACTIVE + qualityProvider.getId().toLowerCase();
    }
    
    public static boolean isQualityProviderActive(Project project, QualityProvider qualityProvider) {
        // disable quality provider if the project has no registered Session
        if (null == project.getLookup().lookup(qualityProvider.getQualitySessionClass())) {
            return false;
        }
        return NbPreferences.forModule(QualityProvider.class).node("project").getBoolean(project.getProjectDirectory() + "." + getPropertyNameActive(qualityProvider), true);
    }

    public static void setQualityProviderActive(Project project, QualityProvider qualityProvider, boolean active) {
        
        NbPreferences.forModule(QualityProvider.class).node("project").putBoolean(project.getProjectDirectory() + "." + getPropertyNameActive(qualityProvider), active);
        
        firePropertyChange(getPropertyNameActive(qualityProvider), null, active);
    }

    public static boolean isQualityProviderAnnotateActive(Project project, QualityProvider qualityProvider) {
        // disable quality provider if the project has no registered Session
        if (null == project.getLookup().lookup(qualityProvider.getQualitySessionClass())) {
            return false;
        }
        return NbPreferences.forModule(QualityProvider.class).node("project").getBoolean(project.getProjectDirectory() + "." + getPropertyNameAnnotateActive(qualityProvider), false);
    }

    public static void setQualityProviderAnnotateActive(Project project, QualityProvider qualityProvider, boolean active) {
        NbPreferences.forModule(QualityProvider.class).node("project").putBoolean(project.getProjectDirectory() + "." + getPropertyNameAnnotateActive(qualityProvider), active);
        firePropertyChange(getPropertyNameAnnotateActive(qualityProvider), null, active);
    }
    
}
