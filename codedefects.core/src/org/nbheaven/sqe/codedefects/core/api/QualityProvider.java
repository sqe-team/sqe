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

import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;

import org.openide.util.Lookup;

import javax.swing.Icon;
import org.nbheaven.sqe.codedefects.core.spi.AbstractQualityProvider;

/**
 *
 * @author Sven Reimers
 */
public interface QualityProvider extends Lookup.Provider {

    public String getId();

    public String getDisplayName();

    public Icon getIcon();

    public boolean isValidFor(Project project);

    public Class<? extends QualitySession> getQualitySessionClass();

    public QualitySession createQualitySession(Project project);

    public SessionEventProxy getSessionEventProxy();

    public static SessionEventProxy getGlobalSessionEventProxy() {
        return AbstractQualityProvider.getGlobalSessionEventProxy();
    }

    public static interface SessionEventProxy {

        public String ENABLED_PROPERTY = "enabled";
        public String ANNOTATE_PROJECT_RESULT_ENABLED_PROPERTY = "annotateProjectResultEnabled";
        public String BACKGROUND_SCANNING_ENABLED_PROPERTY = "backgroundScanningEnabled";
        public String ANNOTATE_PROJECT_RESULT_EFFECTIVE_ENABLED_PROPERTY = "annotateProjectResultEffectiveEnabled";
        public String BACKGROUND_SCANNING_EFFECTIVE_ENABLED_PROPERTY = "backgroundScanningEffectiveEnabled";

        public void addPropertyChangeListener(PropertyChangeListener listener);

        public void removePropertyChangeListener(PropertyChangeListener listener);

        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

        public PropertyChangeListener addWeakPropertyChangeListener(PropertyChangeListener listener);

        public PropertyChangeListener addWeakPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    }

}
