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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.actions;

import javax.swing.Action;
import org.nbheaven.sqe.codedefects.ui.actions.AbstractEnableBackgroundScanningAction;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleQualityProvider;
import org.nbheaven.sqe.codedefects.ui.actions.AbstractShowProjectResultAnnotationsAction;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleSession;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Action to show or hide project result annotations.
 *
 * @author Florian Vogler
 */
public final class EnableCheckstyleBackgroundScanningAction extends AbstractEnableBackgroundScanningAction implements ContextAwareAction {

    public EnableCheckstyleBackgroundScanningAction() {
        this(Utilities.actionsGlobalContext());
    }

    public EnableCheckstyleBackgroundScanningAction(CheckstyleSession session) {
        this(SQEProjectSupport.createContextLookup(session.getProject()));
    }

    private EnableCheckstyleBackgroundScanningAction(Lookup context) {
        super(context, CheckstyleQualityProvider.getDefault());

        putValue(Action.NAME, "Enable checkstyle background scanning");//NbBundle.getMessage(ShowCheckstyleProjectResultAnnotationsAction.class, "LBL_CheckstyleAction")); //NOI18N
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/checkstyle/codedefects/core/resources/visible.png")));
        updateActionState();
    }

    @Override
    public EnableCheckstyleBackgroundScanningAction createContextAwareInstance(Lookup context) {
        return new EnableCheckstyleBackgroundScanningAction(context);
    }

}
