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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsQualityProvider;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsSession;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.ui.FindBugsTopComponent;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.nbheaven.sqe.codedefects.ui.UIHandle;
import org.nbheaven.sqe.codedefects.ui.actions.AbstractQualitySessionAwareAction;
import org.nbheaven.sqe.codedefects.ui.utils.UiUtils;
import org.nbheaven.sqe.core.api.SQEManager;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action to trigger findbugs run on actual project
 *
 * @author Florian Vogler
 */
public class RunFindBugsAction extends AbstractQualitySessionAwareAction implements ContextAwareAction {

    private final QualityProvider provider;

    public RunFindBugsAction() {
        this(Utilities.actionsGlobalContext());
    }

    private RunFindBugsAction(Lookup context) {
        super(context, FindBugsQualityProvider.getDefault().getSessionEventProxy());
        this.provider = FindBugsQualityProvider.getDefault();

        //        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putValue(Action.NAME, NbBundle.getMessage(RunFindBugsAction.class, "LBL_RunFindBugsAction")); //NOI18N
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/findbugs.png")));
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new RunFindBugsAction(context);
    }

    @Override
    protected void updateActionStateImpl(Project project) {
        QualitySession session = SQECodedefectSupport.retrieveSession(project, provider);
        setEnabled(isEnabledForProject(project));
    }

    @Override
    protected boolean isEnabledForProject(Project project) {
        return SQECodedefectSupport.isQualityProviderEnabled(project, provider)
                && provider.isValidFor(project);
    }

    @Override
    protected void actionPerformedImpl(ActionEvent e, Project project) {
        QualitySession session = SQECodedefectSupport.retrieveSession(project, provider);
        session.computeResult();

//        SQEManager.getDefault().setActiveProject(project);
        UIHandle uiHandle = UiUtils.getUIHandle(provider);
        if (null != uiHandle) {
            uiHandle.open();
        }
    }

}
