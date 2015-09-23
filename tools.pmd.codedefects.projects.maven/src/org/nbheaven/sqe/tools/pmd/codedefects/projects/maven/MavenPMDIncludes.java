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
package org.nbheaven.sqe.tools.pmd.codedefects.projects.maven;

import java.util.Collection;
import java.util.HashSet;
import org.nbheaven.sqe.core.java.utils.FileObjectUtilities;
import org.nbheaven.sqe.core.java.utils.ProjectUtilities;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.nbheaven.sqe.core.maven.utils.MavenUtilities;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDIncludes;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Klähn
 */
class MavenPMDIncludes implements PMDIncludes {
    private final Project project;
    //hack: easiest way to figure of the test source root without too
    // much calculations..
    public static final String NAME_TESTSOURCE = "2TestSourceRoot"; //NOI18N

    public MavenPMDIncludes(Project p) {
        project = p;
    }

    @Override
    public Collection<FileObject> getProjectIncludes() {
        MavenPluginConfiguration mpc = MavenUtilities.getReportPluginConfiguration(project, "org.apache.maven.plugins", "maven-pmd-plugin");
        String val = mpc.getValue("includeTests");
        //default value of parameter is false;
        boolean includeTests = val != null && Boolean.parseBoolean(val);
        //TODO more include/exclude parameters are included in configuration..

        SourceGroup[] groups = ProjectUtilities.getJavaSourceGroups(project);
        Collection<FileObject> toRet = new HashSet<FileObject>();
        for (SourceGroup g : groups) {
            if (!includeTests && NAME_TESTSOURCE.equals(g.getName())) {
                continue;
            }
            toRet = FileObjectUtilities.collectAllJavaSourceFiles(g.getRootFolder(), toRet);
        }
        return toRet;
    }

}
