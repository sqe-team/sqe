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

package org.nbheaven.sqe.core.maven.utils;

import org.apache.maven.project.MavenProject;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.util.Lookup;

/**
 * A proxy of ModelHandle class for use in customizers in projects
 * that don't have a friend dependency to maven module
 * @author mkleint
 */
public class ModelHandleProxy {
    private final Project p;

    private ModelHandleProxy(Project p) {
        this.p = p;
    }

    public static ModelHandleProxy create(Lookup customizerLookup) {
        Project p = customizerLookup.lookup(Project.class);
        assert p != null;
        return new ModelHandleProxy(p);
    }

    public MavenPluginConfiguration getMavenPluginConfig(String groupId, String artifactId) {
        NbMavenProject nbmp = p.getLookup().lookup(NbMavenProject.class);
        if (nbmp != null) {
            MavenProject mp = nbmp.getMavenProject();
            if (mp != null) {
                return MavenUtilities.getReportPluginConfigurationImpl(mp, groupId, artifactId);
            } // else impossible in NB 7.0
        }
        return new MavenPluginConfiguration() {
            public boolean isDefinedInProject() {
                return false;
            }
            public String getValue(String path) {
                return null;
            }
            public String[] getStringListValue(String listParent, String listChild) {
                return new String[0];
            }
        };
    }

}
