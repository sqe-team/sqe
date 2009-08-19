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
package org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.widgets;

import java.awt.Color;
import java.awt.Font;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.model.DepFinderModel;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.DependencyScene;
import static org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.DependencyScene.*;
import org.nbheaven.sqe.core.java.utils.Scope;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author fvo
 */
public class PackageWidget extends DataObjectWidget {

    private static final Border BORDER_PROJECT = BorderFactory.createRoundedBorder(10, 10, 5, 5, Color.LIGHT_GRAY, Color.BLACK);
    private static final Border BORDER_EXTERNAL = BorderFactory.createRoundedBorder(10, 10, 5, 5, Color.LIGHT_GRAY, Color.BLACK);
    private static final Border BORDER_JDK = BorderFactory.createRoundedBorder(10, 10, 5, 5, Color.WHITE, Color.LIGHT_GRAY);
    private static final Border BORDER_MIXED = BorderFactory.createRoundedBorder(10, 10, 5, 5, Color.WHITE, Color.RED);

    public PackageWidget(Scene scene, String packageDataObject) {
        super(scene);
        updateUI(packageDataObject);

    }

    @Override
    public void updateUI() {
        DependencyScene scene = (DependencyScene) getScene();
        String packageDataObject = (String) scene.findObject(this);
        updateUI(packageDataObject);
    }

    private void updateUI(String packageName) {
        DependencyScene scene = (DependencyScene) getScene();
        DepFinderModel model = scene.getModel();

        Scope scope = model.getPackageScope(decodePackageName(packageName));
        setLabel(packageName);
        if (null != scope) {
            switch (scope) {
                case PROJECT:
                    setFont(getScene().getDefaultFont().deriveFont(Font.BOLD));
                    setBorder(BORDER_PROJECT);
                    break;
                case EXTERNAL:
                    setBorder(BORDER_EXTERNAL);
                    break;
                case JDK:
                    setBorder(BORDER_JDK);
                    break;
                case MIXED:
                    setBorder(BORDER_MIXED);
                    break;
            }
        }
    }
}
