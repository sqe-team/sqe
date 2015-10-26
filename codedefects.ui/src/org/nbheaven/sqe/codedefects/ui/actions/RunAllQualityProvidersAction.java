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
package org.nbheaven.sqe.codedefects.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.spi.SQEUtilities;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.nbheaven.sqe.codedefects.ui.UIHandle;
import org.nbheaven.sqe.codedefects.ui.utils.UiUtils;
import org.netbeans.api.project.Project;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Florian Vogler
 */
public final class RunAllQualityProvidersAction extends AbstractQualitySessionAwareAction implements ContextAwareAction, Presenter.Toolbar {

    private static final RequestProcessor REQUEST_PROCESSOR = new RequestProcessor(RunAllQualityProvidersAction.class);

    public RunAllQualityProvidersAction() {
        this(Utilities.actionsGlobalContext());
    }

    private RunAllQualityProvidersAction(Lookup context) {
        super(context, QualityProvider.getGlobalSessionEventProxy());
        this.putValue(Action.NAME, NbBundle.getMessage(RunAllQualityProvidersAction.class, "LBL_RunAllQualityProvidersAction")); //NOI18N
        this.putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/codedefects/ui/resources/sqe.png")));
        updateActionState();
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new RunAllQualityProvidersAction(context);
    }

    @Override
    protected void updateActionStateImpl(Project project) {
        setEnabled(isEnabledForProject(project));
    }

    @Override
    protected boolean isEnabledForProject(Project project) {
        for (QualityProvider provider : SQEUtilities.getProviders()) {
            QualitySession session = SQECodedefectSupport.retrieveSession(project, provider);
            if (isEnabledForProject(project, provider)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnabledForProject(Project project, QualityProvider provider) {
        return SQECodedefectSupport.isQualityProviderEnabled(project, provider) && provider.isValidFor(project);
    }

    @Override
    protected void actionPerformedImpl(ActionEvent e, Project project) {
        if (null != project) {
            SQEUtilities.getProviders().stream()
                    .filter((provider) -> (isEnabledForProject(project, provider)))
                    .map((provider) -> new ComputeResultTask(project, provider))
                    .forEach((computeResultTask) -> REQUEST_PROCESSOR.post(computeResultTask));
        }
    }

    @Override
    public Component getToolbarPresenter() {
        JToggleButton button = new JToggleButton(this);
        button.setHideActionText(true);
        button.setRolloverEnabled(false);
        button.setFocusable(false);
        return button;
    }

    private final static class ComputeResultTask extends Task {

        private final QualitySession qualitySession;
        private final QualityProvider qualityProvider;

        private ComputeResultTask(Project project, QualityProvider provider) {
            this.qualityProvider = provider;
            this.qualitySession = SQECodedefectSupport.retrieveSession(project, provider);
        }

        @Override
        public final void run() {
            qualitySession.computeResult();
            SwingUtilities.invokeLater(new UIUpdateTask(qualityProvider));
        }
    }

    private static class UIUpdateTask implements Runnable {

        private final QualityProvider qualityProvider;

        private UIUpdateTask(QualityProvider qualityProvider) {
            this.qualityProvider = qualityProvider;
        }

        @Override
        public final void run() {
            UIHandle uiHandle = UiUtils.getUIHandle(qualityProvider);
            if (null != uiHandle) {
                uiHandle.open();
            }
        }
    }
}
