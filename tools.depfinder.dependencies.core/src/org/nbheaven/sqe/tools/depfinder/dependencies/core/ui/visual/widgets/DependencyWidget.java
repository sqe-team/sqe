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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.model.DepFinderModel;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.model.DependencyNode;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.DependencyScene;
import org.nbheaven.sqe.core.java.utils.Scope;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import static org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.DependencyScene.*;

/**
 *
 * @author fvo
 */
public class DependencyWidget extends ConnectionWidget {

    public DependencyWidget(Scene scene, DependencyNode dependencyDataObject) {
        super(scene);
        updateUI(dependencyDataObject);
    }

    public void updateUI() {
        DependencyScene scene = (DependencyScene) getScene();
        DependencyNode dependencyDataObject = (DependencyNode) scene.findObject(this);
        updateUI(dependencyDataObject);
    }

    private void updateUI(DependencyNode dependencyDataObject) {
        DependencyScene scene = (DependencyScene) getScene();
        DepFinderModel model = scene.getModel();

        String source = decodePackageName(dependencyDataObject.getSource());
        String target = decodePackageName(dependencyDataObject.getTarget());

        Scope sourceScope = model.getPackageScope(source);
        Scope targetScope = model.getPackageScope(target);


        if (sourceScope == Scope.JDK || targetScope == Scope.JDK) {
            setLineColor(Color.LIGHT_GRAY);

        } else if (sourceScope == Scope.PROJECT && targetScope == Scope.PROJECT) {
            setLineColor(Color.RED);
        }
    }
}
