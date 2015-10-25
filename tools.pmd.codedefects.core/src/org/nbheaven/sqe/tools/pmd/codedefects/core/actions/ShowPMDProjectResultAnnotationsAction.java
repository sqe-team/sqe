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
package org.nbheaven.sqe.tools.pmd.codedefects.core.actions;

import javax.swing.Action;
import static javax.swing.Action.SMALL_ICON;
import org.nbheaven.sqe.codedefects.ui.actions.AbstractShowProjectResultAnnotationsAction;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDQualityProvider;
import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDSession;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Action to show or hide project result annotations.
 *
 * @author Florian Vogler
 */
public final class ShowPMDProjectResultAnnotationsAction extends AbstractShowProjectResultAnnotationsAction implements ContextAwareAction {

    public ShowPMDProjectResultAnnotationsAction() {
        this(Utilities.actionsGlobalContext());
    }

    public ShowPMDProjectResultAnnotationsAction(PMDSession session) {
        this(SQEProjectSupport.createContextLookup(session.getProject()));
    }

    private ShowPMDProjectResultAnnotationsAction(Lookup context) {
        super(context, PMDQualityProvider.getDefault());

        putValue(Action.NAME, "Show Annotations");//NbBundle.getMessage(ShowCheckstyleProjectResultAnnotationsAction.class, "LBL_CheckstyleAction")); //NOI18N
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/pmd/codedefects/core/resources/visible.png")));
        updateActionState();
    }

    @Override
    public ShowPMDProjectResultAnnotationsAction createContextAwareInstance(Lookup context) {
        return new ShowPMDProjectResultAnnotationsAction(context);
    }

}
