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
package org.nbheaven.sqe.core.api;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sven
 */
public final class SQEManager implements Lookup.Provider {

    public static final String PROP_ACTIVE_PROJECT = "PropActiveProject";
    public static final String PROP_CURRENT_PROJECT = "PropCurrentProject";
    private static SQEManager sqeManager;
    private final PropertyChangeSupport support;
    private final ActionGlobalContextListner actionGlobalContextListner;
    private final InstanceContent instanceContent;
    private final Lookup lookup;
    private Project activeProject;
    private Node activeProjectNode;
    private Project currentProject;

    public SQEManager() {
        this.support = new PropertyChangeSupport(this);
        this.instanceContent = new InstanceContent();
        this.lookup = new AbstractLookup(instanceContent);
        this.actionGlobalContextListner = new ActionGlobalContextListner(this);
        actionGlobalContextListner.setEnable(true);
    }

    public static synchronized SQEManager getDefault() {
        if (null == sqeManager) {
            sqeManager = new SQEManager();
        }
        return sqeManager;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public boolean getFollowActionGlobalContext() {
        return actionGlobalContextListner.isEnable();
    }

    public void setFollowActionGlobalContext(boolean followActionGlobalContext) {
        actionGlobalContextListner.setEnable(followActionGlobalContext);
    }

    private static class ActionGlobalContextListner implements LookupListener {

        private final SQEManager manager;
        private Result<Project> activeGlobalProject;
        private Result<DataObject> activeGlobalDataObject;

        public ActionGlobalContextListner(SQEManager manager) {
            this.manager = manager;
        }

        public boolean isEnable() {
            return null != activeGlobalProject;
        }

        public synchronized void setEnable(boolean enable) {
            if (enable && null == activeGlobalProject) {
                activeGlobalProject = Utilities.actionsGlobalContext().lookupResult(Project.class);
                activeGlobalProject.addLookupListener(this);
                activeGlobalDataObject = Utilities.actionsGlobalContext().lookupResult(DataObject.class);
                activeGlobalDataObject.addLookupListener(this);
            } else if (!enable && null != activeGlobalProject) {
                activeGlobalProject.removeLookupListener(this);
                activeGlobalDataObject.removeLookupListener(this);
                activeGlobalProject = null;
            }
        }

        @Override
        public void resultChanged(LookupEvent arg0) {
            Project project = null;

            Collection<? extends Project> allProjectInstances = activeGlobalProject.allInstances();
            if (allProjectInstances.size() == 1) {
                project = allProjectInstances.iterator().next();
            }

            if (null == project) {
                Collection<? extends DataObject> allDataObjects = activeGlobalDataObject.allInstances();
                if (allDataObjects.size() == 1) {
                    project = FileOwnerQuery.getOwner(allDataObjects.iterator().next().getPrimaryFile());
                }
            }

            manager.setActiveProject(project);
        }
    }

    public Project getActiveProject() {
        return activeProject;
    }

    public Project getCurrentProject() {
        return currentProject;
    }

    public void setActiveProject(Project project) {
        if (this.activeProject == project) {
            return;
        }
        if (null != this.activeProject) {
            instanceContent.remove(activeProject);
            instanceContent.remove(activeProjectNode);
        }

        Project oldProject = this.activeProject;
        activeProject = project;
        activeProjectNode = null;

        if (null != this.activeProject) {
            activeProjectNode = new AbstractNode(Children.LEAF, Lookups.singleton(activeProject));
            instanceContent.add(activeProjectNode);
            instanceContent.add(activeProject);
        }

        firePropertyChange(PROP_ACTIVE_PROJECT, oldProject, this.activeProject);
    }

    public void setCurrentProject(Project currentProject) {
        Project oldProject = this.currentProject;
        this.currentProject = currentProject;
        firePropertyChange(PROP_CURRENT_PROJECT, oldProject, this.currentProject);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }

    protected final void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        if (EventQueue.isDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    firePropertyChange(propertyName, oldValue, newValue);
                }
            });
            return;
        }
        support.firePropertyChange(propertyName, oldValue, newValue);
    }
}