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
package org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.SwingUtilities;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.model.DepFinderModel;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.model.DependencyNode;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.widgets.PackageWidget;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.model.ModelRef;
import org.nbheaven.sqe.tools.depfinder.dependencies.core.ui.visual.widgets.DependencyWidget;
import org.nbheaven.sqe.core.java.utils.Scope;
import org.nbheaven.sqe.core.ui.components.filter.FilterType;
import org.nbheaven.sqe.core.ui.components.filter.JFilter;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.RequestProcessor;

/**
 *
 * @author fvo
 */
public class DependencyScene extends GraphScene<String, DependencyNode> {

    private static final String DEFAULT_PACKAGE = "<default>";
    private final ModelRef modelRef;
    private final SceneLayout sceneLayout;
    private final LayerWidget projectPackagesLayer;
    private final LayerWidget projectDependenciesLayer;
    private final LayerWidget externalPackagesLayer;
    private final LayerWidget externalDependenciesLayer;
    private final LayerWidget jdkPackagesLayer;
    private final LayerWidget jdkDependenciesLayer;
    private final LayerWidget interactiveLayer;
    private final WidgetAction moveAction;
    private boolean showExternalScope = false;
    private boolean showJdkScope = false;

    public DependencyScene(ModelRef modelRef) {
        this.modelRef = modelRef;

        projectPackagesLayer = new LayerWidget(this);
        projectDependenciesLayer = new LayerWidget(this);

        externalPackagesLayer = new LayerWidget(this);
        externalPackagesLayer.setVisible(showExternalScope);
        externalDependenciesLayer = new LayerWidget(this);
        externalDependenciesLayer.setVisible(showExternalScope);

        jdkPackagesLayer = new LayerWidget(this);
        jdkPackagesLayer.setVisible(showJdkScope);
        jdkDependenciesLayer = new LayerWidget(this);
        jdkDependenciesLayer.setVisible(showJdkScope);

        interactiveLayer = new LayerWidget(this);

        addChild(jdkPackagesLayer);
        addChild(externalPackagesLayer);
        addChild(projectPackagesLayer);

        addChild(jdkDependenciesLayer);
        addChild(externalDependenciesLayer);
        addChild(projectDependenciesLayer);

        addChild(interactiveLayer);

        getActions().addAction(ActionFactory.createZoomAction());

        moveAction = ActionFactory.createMoveAction();


        GridGraphLayout<String, DependencyNode> layout = new GridGraphLayout<String, DependencyNode>();
        layout.setChecker(true);
        layout.setAnimated(false);
        sceneLayout = LayoutFactory.createSceneGraphLayout(this, layout);
    }

    public boolean isShowExternalScope() {
        return showExternalScope;
    }

    public void setShowExternalScope(boolean showExternalScope) {
        if (this.showExternalScope ^ showExternalScope) {
            this.showExternalScope = showExternalScope;
            externalPackagesLayer.setVisible(showExternalScope);
            externalDependenciesLayer.setVisible(showExternalScope);
            validate();
            getView().repaint();
        }
    }

    public boolean isShowJdkScope() {
        return showJdkScope;
    }

    public void setShowJDKScope(boolean showJDKScope) {
        if (this.showJdkScope ^ showJDKScope) {
            this.showJdkScope = showJDKScope;
            jdkPackagesLayer.setVisible(showJDKScope);
            jdkDependenciesLayer.setVisible(showJDKScope);
            validate();
            getView().repaint();
        }
    }

    public ModelRef getModelRef() {
        return modelRef;
    }

    public DepFinderModel getModel() {
        return modelRef.getModel();
    }

    public static String encodePackageName(String packageName) {
        String name = null == packageName ? "" : packageName.trim();
        if (name.length() == 0) {
            return DEFAULT_PACKAGE;
        }
        return name;
    }

    public static String decodePackageName(String packageName) {
        if (DEFAULT_PACKAGE == packageName || DEFAULT_PACKAGE.equals(packageName)) {
            return "";
        }
        return packageName;
    }

    public void filterChanged(JFilter jFilter) {
        FilterType type = jFilter.getFilterType();
        String[] rules = jFilter.getFilterStrings();
        for (String node : getNodes()) {
            boolean show = type.accept(node, rules);
            Widget nodeWidget = findWidget(node);
            nodeWidget.setVisible(show);

            for (DependencyNode dependency : findNodeEdges(node, true, false)) {
                Widget edgeWidget = findWidget(dependency);
                if (show ^ edgeWidget.isVisible()) {
                    String targetNode = getEdgeTarget(dependency);
                    Widget targetWidget = findWidget(targetNode);
                    edgeWidget.setVisible(show ? targetWidget.isVisible() : false);
                }
            }

            for (DependencyNode dependency : findNodeEdges(node, false, true)) {
                Widget edgeWidget = findWidget(dependency);
                if (show ^ edgeWidget.isVisible()) {
                    String targetNode = getEdgeSource(dependency);
                    Widget targetWidget = findWidget(targetNode);
                    edgeWidget.setVisible(show ? targetWidget.isVisible() : false);
                }
            }
        }
        doSceneLayout();
    }

    public void doSceneLayout() {
        sceneLayout.invokeLayout();
    }

    @Override
    protected Widget attachNodeWidget(String node) {
        PackageWidget nodeWidget = new PackageWidget(this, node);
        nodeWidget.getActions().addAction(moveAction);
        Scope scope = getModel().getPackageScope(decodePackageName(node));
        switch (scope) {
            case PROJECT:
            case MIXED:
                projectPackagesLayer.addChild(nodeWidget);
                break;
            case EXTERNAL:
                externalPackagesLayer.addChild(nodeWidget);
                break;
            case JDK:
                jdkPackagesLayer.addChild(nodeWidget);
                break;
        }
        return nodeWidget;
    }

    @Override
    protected Widget attachEdgeWidget(DependencyNode dependency) {
        DependencyWidget dependencyWidget = new DependencyWidget(this, dependency);
        dependencyWidget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);

        Scope targetScope = getModel().getPackageScope(decodePackageName(dependency.getTarget()));

        switch (targetScope) {
            case PROJECT:
            case MIXED:
                projectDependenciesLayer.addChild(dependencyWidget);
                break;
            case EXTERNAL:
                externalDependenciesLayer.addChild(dependencyWidget);
                break;
            case JDK:
                jdkDependenciesLayer.addChild(dependencyWidget);
                break;
        }
        return dependencyWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(DependencyNode dependency, String oldNode, String newNode) {
        PackageWidget nodeWidget = (PackageWidget) findWidget(newNode);
        ConnectionWidget dependencyWidget = (ConnectionWidget) findWidget(dependency);
        dependencyWidget.setSourceAnchor(null == nodeWidget ? null : nodeWidget.getAnchor());
    }

    @Override
    protected void attachEdgeTargetAnchor(DependencyNode dependency, String oldNode, String newNode) {
        PackageWidget nodeWidget = (PackageWidget) findWidget(newNode);
        ConnectionWidget dependencyWidget = (ConnectionWidget) findWidget(dependency);
        dependencyWidget.setTargetAnchor(null == nodeWidget ? null : nodeWidget.getAnchor());
    }
}
