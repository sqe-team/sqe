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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.spi.SQEUtilities;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.codedefects.ui.UIHandle;
import org.nbheaven.sqe.codedefects.ui.utils.UiUtils;
import org.netbeans.api.project.Project;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.Utilities;

/**
 *
 * @author Sven Reimers
 */
public class RunAllQualityProvidersAction extends AbstractAction implements LookupListener, ContextAwareAction, PropertyChangeListener {

    private Lookup context;
    private Lookup.Result<Project> lkpInfo;

    public RunAllQualityProvidersAction() {
        this(Utilities.actionsGlobalContext());
    }

    private RunAllQualityProvidersAction(Lookup context) {
        //        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(Action.NAME, NbBundle.getMessage(RunAllQualityProvidersAction.class, "LBL_RunAllQualityProvidersAction")); //NOI18N
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/codedefects/ui/resources/sqe.png")));
        this.context = context;
    }

    public Action createContextAwareInstance(Lookup context) {
        return new RunAllQualityProvidersAction(context);
    }

    @Override
    public boolean isEnabled() {
        init();
        return super.isEnabled();
    }

    private void init() {
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        if (lkpInfo != null) {
            return;
        }
        SQECodedefectProperties.addPropertyChangeListener(this); //TODO Make weak !!!

        //The thing we want to listen for the presence or absence of
        //on the global selection
        Lookup.Template<Project> tpl = new Lookup.Template<Project>(Project.class);
        lkpInfo = context.lookup(tpl);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    public void resultChanged(LookupEvent ev) {
        updateEnableState();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        updateEnableState();
    }

    private void updateEnableState() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    updateEnableState();
                }
            });
            return;
        }
        setEnabled(areQualityProvidersActive(getActiveProject()));
    }

    private Project getActiveProject() {
        Collection<? extends Project> projects = lkpInfo.allInstances();
        if (projects.size() == 1) {
            Project project = projects.iterator().next();
            return project;
        }
        return null;
    }

    private static boolean areQualityProvidersActive(Project project) {
        if (null != project) {
            for (QualityProvider provider : SQEUtilities.getProviders()) {
                if (provider.isValidFor(project) && SQECodedefectProperties.isQualityProviderActive(project, provider)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void actionPerformed(ActionEvent e) {
        Project project = getActiveProject();
        if (null != project) {
            for (QualityProvider provider : SQEUtilities.getProviders()) {
                if (provider.isValidFor(project) && SQECodedefectProperties.isQualityProviderActive(project, provider)) {
                    ComputeResultTask computeResultTask = new ComputeResultTask(project, provider);
                    RequestProcessor.getDefault().post(computeResultTask);
                }
            }
        }
    }

    private final static class ComputeResultTask extends Task {

        private final QualitySession qualitySession;
        private final QualityProvider qualityProvider;

        private ComputeResultTask(Project project, QualityProvider provider) {
            this.qualityProvider = provider;
            this.qualitySession = project.getLookup().lookup(provider.getQualitySessionClass());
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

        public final void run() {
            UIHandle uiHandle = UiUtils.getUIHandle(qualityProvider);
            if (null != uiHandle) {
                uiHandle.open();
            }
        }
    }
}
