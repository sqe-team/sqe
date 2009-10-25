/* Copyright 2009 Milos Kleint
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

package org.nbheaven.sqe.tools.pmd.codedefects.core.settings.impl;

import java.util.Collection;
import java.util.HashSet;
import org.nbheaven.sqe.core.java.utils.FileObjectUtilities;
import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDIncludes;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
public class DefaultPMDIncludes implements PMDIncludes {
    private final Project project;

    public DefaultPMDIncludes(Project prj) {
        project = prj;
    }

    public Collection<FileObject> getProjectIncludes() {
        SourceGroup[] groups = ProjectUtilities.getJavaSourceGroups(project);
        Collection<FileObject> toRet = new HashSet<FileObject>();
        for (SourceGroup g : groups) {
            FileObject rootOfSourceFolder = g.getRootFolder();
            toRet = FileObjectUtilities.collectAllJavaSourceFiles(rootOfSourceFolder, toRet);
        }
        return toRet;
    }


}
