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

import java.util.prefs.BackingStoreException;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import java.util.prefs.Preferences;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider.SessionEventProxy;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
public abstract class AbstractQualitySession<P extends QualityProvider, R extends QualityResult> extends ProjectOpenedHook implements QualitySession {

    private final Project project;
    private final AbstractQualityProvider provider;

    private final AnnotationControler annotationControler = new AnnotationControler(this);

    private final BooleanProperty enabledProperty = new SimpleBooleanProperty(this, SessionEventProxy.ENABLED_PROPERTY);
    private final BooleanProperty annotateProjectResultEnabledProperty = new SimpleBooleanProperty(this, SessionEventProxy.ANNOTATE_PROJECT_RESULT_ENABLED_PROPERTY);
    private final BooleanProperty backgroundScanningEnabledProperty = new SimpleBooleanProperty(this, SessionEventProxy.BACKGROUND_SCANNING_ENABLED_PROPERTY);

    private final BooleanBinding annotateProjectResultEffectiveEnabledProperty = enabledProperty.and(annotateProjectResultEnabledProperty);
    private final BooleanBinding backgroundScanningEffectiveEnabledProperty = enabledProperty.and(backgroundScanningEnabledProperty);

    private final ObjectProperty<R> resultProperty = new SimpleObjectProperty<>();

    public <PI extends AbstractQualityProvider & QualityProvider> AbstractQualitySession(final PI provider, final Project project) {
        this.provider = provider;
        this.project = project;

        enabledProperty.addListener((source, oldValue, newValue)
                -> this.provider.fireSessionPropertyChange(this, enabledProperty.getName(), oldValue, newValue));

        annotateProjectResultEnabledProperty.addListener((source, oldValue, newValue)
                -> this.provider.fireSessionPropertyChange(this, annotateProjectResultEnabledProperty.getName(), oldValue, newValue));

        backgroundScanningEnabledProperty.addListener((source, oldValue, newValue)
                -> this.provider.fireSessionPropertyChange(this, backgroundScanningEnabledProperty.getName(), oldValue, newValue));

        annotateProjectResultEffectiveEnabledProperty.addListener((source, oldValue, newValue)
                -> this.provider.fireSessionPropertyChange(this, SessionEventProxy.ANNOTATE_PROJECT_RESULT_EFFECTIVE_ENABLED_PROPERTY, oldValue, newValue));
        
        backgroundScanningEffectiveEnabledProperty.addListener((source, oldValue, newValue)
                -> this.provider.fireSessionPropertyChange(this, SessionEventProxy.BACKGROUND_SCANNING_EFFECTIVE_ENABLED_PROPERTY, oldValue, newValue)
        );

    }

    @Override
    protected final void projectOpened() {
        Preferences preferences = ProjectUtils.getPreferences(project, provider.getClass(), false);
        enabledProperty.set(preferences.getBoolean(enabledProperty.getName(), true));
        annotateProjectResultEnabledProperty.set(preferences.getBoolean(annotateProjectResultEnabledProperty.getName(), true));
        backgroundScanningEnabledProperty.set(preferences.getBoolean(backgroundScanningEnabledProperty.getName(), true));
        annotationControler.bind();
        
        System.out.println(provider.getDisplayName() + " - Read Property enabled: " + isEnabled());
        System.out.println(provider.getDisplayName() + " - Read Property showAnno: " + isAnnotateProjectResultEnabled());
        System.out.println(provider.getDisplayName() + " - Read Property scanning: " + isBackgroundScanningEnabled());
    }

    @Override
    protected final void projectClosed() {
        Preferences preferences = ProjectUtils.getPreferences(project, provider.getClass(), false);
        preferences.putBoolean(enabledProperty.getName(), enabledProperty.get());
        preferences.putBoolean(annotateProjectResultEnabledProperty.getName(), annotateProjectResultEnabledProperty.get());
        preferences.putBoolean(backgroundScanningEnabledProperty.getName(), backgroundScanningEnabledProperty.get());
        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        annotationControler.unbind();
    }

    @Override
    public String getName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return ProjectUtils.getInformation(project).getName();
    }

    @Override
    public final Project getProject() {
        return this.project;
    }

    @Override
    public final P getProvider() {
        return (P) provider;
    }

    @Override
    public final BooleanProperty getEnabledProperty() {
        return enabledProperty;
    }

    @Override
    public final boolean isEnabled() {
        return getEnabledProperty().get();
    }

    @Override
    public final void setEnabled(boolean enabled) {
        getEnabledProperty().set(enabled);
    }

    @Override
    public final BooleanProperty getAnnotateProjectResultEnabledProperty() {
        return annotateProjectResultEnabledProperty;
    }

    @Override
    public final boolean isAnnotateProjectResultEnabled() {
        return getAnnotateProjectResultEnabledProperty().get();
    }

    @Override
    public final void setAnnotateProjectResultEnabled(boolean enabled) {
        getAnnotateProjectResultEnabledProperty().set(enabled);
    }

    @Override
    public final BooleanProperty getBackgroundScanningEnabledProperty() {
        return backgroundScanningEnabledProperty;
    }

    @Override
    public final boolean isBackgroundScanningEnabled() {
        return getBackgroundScanningEnabledProperty().get();
    }

    @Override
    public final void setBackgroundScanningEnabled(boolean enabled) {
        getBackgroundScanningEnabledProperty().set(enabled);
    }

    @Override
    public final ObservableObjectValue<? extends R> getResultProperty() {
        return resultProperty;
    }

    @Override
    public final BooleanBinding getAnnotateProjectResultEffectiveEnabledProperty() {
        return annotateProjectResultEffectiveEnabledProperty;
    }

    @Override
    public final boolean isAnnotateProjectResultEffectiveEnabled() {
        return getAnnotateProjectResultEffectiveEnabledProperty().get();
    }

    @Override
    public final BooleanBinding getBackgroundScanningEffectiveEnabledProperty() {
        return backgroundScanningEffectiveEnabledProperty;
    }

    @Override
    public final boolean isBackgroundScanningEffectiveEnabled() {
        return getBackgroundScanningEffectiveEnabledProperty().get();
    }

    @Override
    public final R getResult() {
        return getResultProperty().get();
    }

    protected final void setResult(R result) {
        resultProperty.setValue(result);
    }

}
