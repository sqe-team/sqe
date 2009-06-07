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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import org.netbeans.api.project.Project;

/**
 *
 * @author fvo
 */
public class ModelRef {

    private final Reference<Project> projectRef;

    ModelRef(Project project) {
        this.projectRef = new SoftReference<Project>(project);
    }

    public Project getProject() {
        return projectRef.get();
    }

    public DepFinderModel getModel() {
        Project project = projectRef.get();
        assert null != project : "Project was closed";
        return ModelManager.getDefault().getModel(project);
    }
}
