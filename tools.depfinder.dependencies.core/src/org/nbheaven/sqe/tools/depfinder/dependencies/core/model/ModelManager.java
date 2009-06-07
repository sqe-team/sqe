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

import java.util.Map;
import java.util.WeakHashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.netbeans.api.project.Project;

/**
 *
 * @author fvo
 */
public class ModelManager {

    private static ModelManager instance;
    private Map<Project, DepFinderModel> project2Model = new WeakHashMap<Project, DepFinderModel>();

    public static ModelManager getDefault() {
        if (null == instance) {
            synchronized (ModelManager.class) {
                if (null == instance) {
                    BasicConfigurator.configure();
                    Logger.getRootLogger().setLevel(Level.ERROR);
                    instance = new ModelManager();
                }
            }
        }
        return instance;
    }

    private ModelManager() {
    }

    public ModelRef getModelRef(Project project) {
        return new ModelRef(project);
    }

    Project getProject(DepFinderModel model) {
        for (Map.Entry<Project, DepFinderModel> entry : project2Model.entrySet()) {
            if (model == entry.getValue() || model.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    DepFinderModel getModel(Project project) {
        DepFinderModel model = project2Model.get(project);
        if (null == model) {
            synchronized (this) {
                model = project2Model.get(project);
                if (null == model) {
                    model = new DepFinderModel(this);
                    project2Model.put(project, model);
                }
            }
        }
        return model;
    }
}
