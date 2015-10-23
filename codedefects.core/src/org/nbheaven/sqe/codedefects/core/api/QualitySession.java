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

import javafx.beans.property.BooleanProperty;
import org.netbeans.api.project.Project;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;

/**
 *
 * @author Sven Reimers
 */
public interface QualitySession {

    public String getName();

    public String getDisplayName();

    public Project getProject();

    public QualityProvider getProvider();

    public BooleanProperty getEnabledProperty();

    public boolean isEnabled();

    public void setEnabled(boolean enabled);

    public BooleanProperty getAnnotateProjectResultEnabledProperty();

    public boolean isAnnotateProjectResultEnabled();

    public void setAnnotateProjectResultEnabled(boolean enabled);

    public BooleanProperty getBackgroundScanningEnabledProperty();

    public boolean isBackgroundScanningEnabled();

    public void setBackgroundScanningEnabled(boolean enabled);

    public ObservableBooleanValue getAnnotateProjectResultEffectiveEnabledProperty();

    public boolean isAnnotateProjectResultEffectiveEnabled();

    public ObservableBooleanValue getBackgroundScanningEffectiveEnabledProperty();

    public boolean isBackgroundScanningEffectiveEnabled();

    public ObservableObjectValue<? extends QualityResult> getResultProperty();

    public QualityResult getResult();

    public void computeResult();

    public QualityResult computeResultAndWait();

}
