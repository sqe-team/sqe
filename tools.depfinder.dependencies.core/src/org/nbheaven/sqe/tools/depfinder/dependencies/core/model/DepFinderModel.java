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
package org.nbheaven.sqe.tools.depfinder.dependencies.core.model;

import com.jeantessier.classreader.ClassfileLoader;
import com.jeantessier.classreader.ClassfileLoaderDispatcher;
import com.jeantessier.classreader.ClassfileLoaderEventSource;
import com.jeantessier.classreader.ModifiedOnlyDispatcher;
import com.jeantessier.classreader.Monitor;
import com.jeantessier.classreader.TransientClassfileLoader;
import com.jeantessier.dependency.VisitorBase;
import com.jeantessier.dependency.ClassNode;
import com.jeantessier.dependency.CodeDependencyCollector;
import com.jeantessier.dependency.DeletingVisitor;
import com.jeantessier.dependency.FeatureNode;
import com.jeantessier.dependency.Node;
import com.jeantessier.dependency.NodeFactory;
import com.jeantessier.dependency.PackageNode;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.nbheaven.sqe.core.java.utils.Scope;
import org.nbheaven.sqe.core.java.utils.ScopeUtilities;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;

/**
 *
 * @author fvo
 */
public class DepFinderModel {

    private final ModelManager manager;
    private final NodeFactory factory;
    private final ClassfileLoader loader;
    private final ProgressMonitor progressMonitor;
    private final Map<String, Scope> packageScopeCache = new HashMap<String, Scope>();
    private final Map<String, ArrayList<String>> packageClassesCache = new HashMap<String, ArrayList<String>>();
    private final Map<String, ArrayList<String>> packageOutboundDependenciesCache = new HashMap<String, ArrayList<String>>();
    private final Map<String, ArrayList<String>> classOutboundDependenciesCache = new HashMap<String, ArrayList<String>>();
    private final Map<String, ArrayList<String>> featureOutboundDependenciesCache = new HashMap<String, ArrayList<String>>();
    private final Map<String, Scope> classScopeCache = new HashMap<String, Scope>();
    private final Map<String, ArrayList<String>> classFeatureCache = new HashMap<String, ArrayList<String>>();

    public DepFinderModel(ModelManager manager) {
        this.manager = manager;

        factory = new NodeFactory();

        CodeDependencyCollector collector = new CodeDependencyCollector(factory);

        DeletingVisitor deletingVisitor = new DeletingVisitor(factory);

        Monitor monitor = new Monitor(collector, deletingVisitor);
        monitor.setClosedSession(true);

        ClassfileLoaderDispatcher dispatcher = new ModifiedOnlyDispatcher(ClassfileLoaderEventSource.DEFAULT_DISPATCHER);
        progressMonitor = new ProgressMonitor("Analyze dependencies");

        loader = new TransientClassfileLoader(dispatcher);
        loader.addLoadListener(monitor);
        loader.addLoadListener(progressMonitor);
    }

    public ModelManager getManager() {
        return manager;
    }

    public synchronized void update() {
        Project project = getManager().getProject(this);
        Collection<String> binaryFiles = ProjectUtilities.findBinaryRoots(project);
        progressMonitor.startMonitoring(binaryFiles);
        try {
            clearCaches();
            loader.load(binaryFiles);
        } finally {
            progressMonitor.stopMonitoring();
        }
    }

    private void clearCaches() {
        packageScopeCache.clear();
        packageClassesCache.clear();
        classScopeCache.clear();
        classFeatureCache.clear();
        packageOutboundDependenciesCache.clear();
    }

    public Collection<String> getPackages() {
        return factory.getPackages().keySet();
    }

    public Collection<String> getOutboundPackageDependencies(String packageName) {
        PackageNode packageNode = factory.getPackages().get(packageName);

        if (null == packageNode) {
            return Collections.emptyList();
        }

        ArrayList<String> list = packageOutboundDependenciesCache.get(packageName);

        if (null == list) {
            OutboundDependenciesVisitor visitor = new OutboundDependenciesVisitor(packageNode).traverseNode();
            list = new ArrayList<String>(visitor.getPackages());
            list.trimToSize();
            packageOutboundDependenciesCache.put(packageName, list);
        }
        return list;
    }

    public Collection<String> getPackageClasses(String packageNsme) {
        PackageNode packageNode = factory.getPackages().get(packageNsme);
        if (null == packageNode) {
            return Collections.emptyList();
        }

        Collection<ClassNode> classes = packageNode.getClasses();

        if (null == classes || classes.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<String> list = packageClassesCache.get(packageNsme);

        if (null == list) {

            list = new ArrayList<String>(classes.size());
            for (ClassNode classNode : classes) {
                list.add(classNode.getName());
            }
            list.trimToSize();
            packageClassesCache.put(packageNsme, list);
        }
        return list;
    }

    public Scope getPackageScope(String packageNsme) {
        Scope packageScope = packageScopeCache.get(packageNsme);
        if (null == packageScope) {
            PackageNode packageNode = factory.getPackages().get(packageNsme);
            for (ClassNode classNode : packageNode.getClasses()) {
                Scope classScope = getClassScope(classNode.getName());
                if (packageScope == Scope.PROJECT && classScope != packageScope) {
                    packageScope = Scope.MIXED;
                } else if (packageScope == Scope.JDK && classScope != packageScope) {
                    packageScope = Scope.MIXED;
                } else if (packageScope == Scope.EXTERNAL && classScope != packageScope) {
                    packageScope = Scope.MIXED;
                } else {
                    packageScope = classScope;
                }
                if (packageScope == Scope.EXTERNAL) {
                    break;
                }
            }
        }
        return packageScope;
    }

    private PackageNode getPackageNode(Node node) {
        PackageNode packageNode;
        if (node instanceof FeatureNode) {
            FeatureNode featureNode = (FeatureNode) node;
            packageNode = featureNode.getClassNode().getPackageNode();
        } else if (node instanceof ClassNode) {
            ClassNode classNode = (ClassNode) node;
            packageNode = classNode.getPackageNode();
        } else {
            packageNode = (PackageNode) node;
        }
        return packageNode;
    }

    public Collection<String> getClasses() {
        return factory.getClasses().keySet();
    }

    public Collection<String> getOutboundClassDependencies(String className) {
        ClassNode classNode = factory.getClasses().get(className);

        if (null == classNode) {
            return Collections.emptyList();
        }

        ArrayList<String> list = classOutboundDependenciesCache.get(className);

        if (null == list) {
            OutboundDependenciesVisitor visitor = new OutboundDependenciesVisitor(classNode).traverseNode();
            list = new ArrayList<String>(visitor.getClasses());
            list.trimToSize();
            classOutboundDependenciesCache.put(className, list);
        }
        return list;
    }

    public String getClassPackage(String className) {
        ClassNode classNode = factory.getClasses().get(className);
        PackageNode packageNode = (null != classNode) ? classNode.getPackageNode() : null;
        return (null != packageNode) ? packageNode.getName() : null;
    }

    public Collection<String> getClassFeatures(String className) {
        ClassNode classNode = factory.getClasses().get(className);

        if (null == classNode) {
            return Collections.emptyList();
        }

        Collection<FeatureNode> features = classNode.getFeatures();

        if (null == features || features.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<String> list = classFeatureCache.get(className);

        if (null == list) {

            list = new ArrayList<String>(features.size());
            for (FeatureNode featureNode : features) {
                list.add(featureNode.getName());
            }
            list.trimToSize();
            classFeatureCache.put(className, list);
        }
        return list;
    }

    public Scope getClassScope(String classNsme) {
        Scope scope = classScopeCache.get(classNsme);
        if (null == scope) {
            scope = ScopeUtilities.findScope(classNsme, getManager().getProject(this));
            classScopeCache.put(classNsme, scope);
        }
        return scope;
    }

    public Collection<String> getFeatures() {
        return factory.getFeatures().keySet();
    }

    public Collection<String> getOutboundFeatureDependencies(String fedatureName) {
        FeatureNode featureNode = factory.getFeatures().get(fedatureName);

        if (null == featureNode) {
            return Collections.emptyList();
        }

        ArrayList<String> list = featureOutboundDependenciesCache.get(fedatureName);

        if (null == list) {
            OutboundDependenciesVisitor visitor = new OutboundDependenciesVisitor(featureNode).traverseNode();
            list = new ArrayList<String>(visitor.getFeatures());
            list.trimToSize();
            featureOutboundDependenciesCache.put(fedatureName, list);
        }
        return list;
    }

    public String getFeatureClasses(String featureName) {
        FeatureNode featureNode = factory.getFeatures().get(featureName);
        ClassNode classNode = (null != featureNode) ? featureNode.getClassNode() : null;
        return (null != classNode) ? classNode.getName() : null;
    }

    public Scope getFeatureScope(String featureName) {
        FeatureNode featureNode = factory.getFeatures().get(featureName);
        return getClassScope(featureNode.getClassNode().getName());
    }

    private static class OutboundDependenciesVisitor extends VisitorBase {

        private final Node node;
        private final boolean tracPackages;
        private final boolean tracClasses;
        private final boolean tracFeatures;
        private Set<String> packages = null;
        private Set<String> classes = null;
        private Set<String> features = null;

        OutboundDependenciesVisitor(Node node) {
            this.node = node;
            tracPackages = node instanceof PackageNode;
            tracClasses = node instanceof ClassNode;
            tracFeatures = node instanceof FeatureNode;
        }

        public Set<String> getPackages() {
            return null == packages ? Collections.<String>emptySet() : packages;
        }

        public Set<String> getClasses() {
            return null == classes ? Collections.<String>emptySet() : classes;
        }

        public Set<String> getFeatures() {
            return null == features ? Collections.<String>emptySet() : features;
        }

        public OutboundDependenciesVisitor traverseNode() {
            traverseNodes(Arrays.asList(node));
            return this;
        }

        @Override
        protected boolean isInScope(PackageNode packageNode) {
            return !tracPackages || node == packageNode;
        }

        @Override
        protected boolean isInScope(ClassNode classNode) {
            return !tracClasses || node == classNode;
        }

        @Override
        protected boolean isInScope(FeatureNode featureNode) {
            return !tracFeatures || node == featureNode;
        }

        @Override
        public void visitOutboundPackageNode(PackageNode packageNode) {
            super.visitOutboundPackageNode(packageNode);
            if (tracPackages) {
                if (null == packages) {
                    packages = new HashSet<String>();
                }
                packages.add(packageNode.getName());
            }
        }

        @Override
        public void visitOutboundClassNode(ClassNode classNode) {
            super.visitOutboundClassNode(classNode);
            if (tracPackages) {
                if (null == packages) {
                    packages = new HashSet<String>();
                }
                packages.add(classNode.getPackageNode().getName());
            }
            if (tracClasses) {
                if (null == classes) {
                    classes = new HashSet<String>();
                }
                classes.add(classNode.getName());
            }
        }

        @Override
        public void visitOutboundFeatureNode(FeatureNode featureNode) {
            super.visitOutboundFeatureNode(featureNode);
            if (tracPackages) {
                if (null == packages) {
                    packages = new HashSet<String>();
                }
                packages.add(featureNode.getClassNode().getPackageNode().getName());
            }
            if (tracClasses) {
                if (null == classes) {
                    classes = new HashSet<String>();
                }
                classes.add(featureNode.getClassNode().getName());
            }
            if (tracFeatures) {
                if (null == features) {
                    features = new HashSet<String>();
                }
                features.add(featureNode.getName());
            }
        }

        @Override
        protected void traverseInbound(Collection<? extends Node> nodes) {
        }
    }
}
