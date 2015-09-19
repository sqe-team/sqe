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
package org.nbheaven.sqe.core.maven.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author sven
 */
public final class MavenUtilities {

    private MavenUtilities() {}

    public static MavenPluginConfiguration getReportPluginConfiguration(final Project project, final String groupId, final String artifactId) {
        MavenProject prj = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        return getReportPluginConfigurationImpl(prj, groupId, artifactId);
    }

    static MavenPluginConfiguration getReportPluginConfigurationImpl(final MavenProject project, final String groupId, final String artifactId) {
        return new MavenPluginConfiguration() {
            @Override
            public String getValue(String path) {
                String v = PluginPropertyUtils.getReportPluginProperty(project, groupId, artifactId, path, null);
                return v != null ? v : PluginPropertyUtils.getPluginProperty(project, groupId, artifactId, path, null);
            }

            @Override
            public String[] getStringListValue(String listParent, String listChild) {
                String[] v = PluginPropertyUtils.getReportPluginPropertyList(project, groupId, artifactId, listParent, listChild, null);
                return v != null ? v : PluginPropertyUtils.getPluginPropertyList(project, groupId, artifactId, listParent, listChild, null);
            }

            @Override
            public boolean isDefinedInProject() {
                return definesReportPlugin(project, groupId, artifactId);
            }

        };
    }


    @SuppressWarnings("deprecation")
    static boolean definesReportPlugin(MavenProject mp, String groupId, String artifactId) {
        return PluginPropertyUtils.getPluginVersion(mp, groupId, artifactId) != null || PluginPropertyUtils.getReportPluginVersion(mp, groupId, artifactId) != null;
    }

//    private static final RequestProcessor RP = new RequestProcessor("Download plugin classpath", 1);

    /**
     * try to collect the plugin's dependency artifacts
     * as defined in <dependencies> section within <plugin>. Will only
     * return files currently in local repository
     *
     * @return list of files in local repository
     */
    public static List<File> findDependencyArtifacts(Project project, String pluginGroupId, String pluginArtifactId, boolean includePluginArtifact) {
        List<File> cpFiles = new ArrayList<File>();
        final NbMavenProject p = project.getLookup().lookup(NbMavenProject.class);
        final MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        MavenProject mp = p.getMavenProject();
        if (includePluginArtifact) {
            Set<Artifact> arts = new HashSet<Artifact>();
            arts.addAll(mp.getReportArtifacts());
            arts.addAll(mp.getPluginArtifacts());
            for (Artifact a : arts) {
                if (pluginArtifactId.equals(a.getArtifactId()) &&
                    pluginGroupId.equals(a.getGroupId())) {
                    File f = a.getFile();
                    if (f == null) {
                        //somehow the report plugins are not resolved, we need to workaround that..
                        f = FileUtil.normalizeFile(new File(new File(online.getLocalRepository().getBasedir()), online.getLocalRepository().pathOf(a)));
                    }
                    if (!f.exists()) {
                        try {
                            online.resolve(a, mp.getRemoteArtifactRepositories(), online.getLocalRepository());
                        } catch (ArtifactResolutionException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (ArtifactNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (f.exists()) {
                        cpFiles.add(f);
                        try {
                            ProjectBuildingRequest req = new DefaultProjectBuildingRequest();
                            req.setRemoteRepositories(mp.getRemoteArtifactRepositories());
                            req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
                            req.setSystemProperties(online.getSystemProperties());
                            ProjectBuildingResult res = online.buildProject(a, req);
                            MavenProject mp2 = res.getProject();
                            if (mp2 != null) {
                                // XXX this is not really right, but mp.dependencyArtifacts = null for some reason
                                for (Dependency dep : mp2.getDependencies()) {
                                    Artifact a2 = online.createArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), "jar");
                                    online.resolve(a2, mp.getRemoteArtifactRepositories(), online.getLocalRepository());
                                    File df = a2.getFile();
                                    if (df.exists()) {
                                        cpFiles.add(df);
                                    }
                                }
                            }
                        } catch (Exception x) {
                            Exceptions.printStackTrace(x);
                        }
                    }
                }
            }
            
        }
        List<Plugin> plugins = mp.getBuildPlugins();
        for (Plugin plug : plugins) {
            if (pluginArtifactId.equals(plug.getArtifactId()) &&
                    pluginGroupId.equals(plug.getGroupId())) {
                try {
                    List<Dependency> deps = plug.getDependencies();
                    ArtifactFactory artifactFactory = online.getPlexus().lookup(ArtifactFactory.class);
                    for (Dependency d : deps) {
                        final Artifact projectArtifact = artifactFactory.createArtifactWithClassifier(d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getType(), d.getClassifier());
                        String localPath = online.getLocalRepository().pathOf(projectArtifact);
                        File f = FileUtil.normalizeFile(new File(online.getLocalRepository().getBasedir(), localPath));
                        if (!f.exists()) {
                            try {
                                online.resolve(projectArtifact, mp.getRemoteArtifactRepositories(), online.getLocalRepository());
                            } catch (ArtifactResolutionException ex) {
                                ex.printStackTrace();
    //                                        Exceptions.printStackTrace(ex);
                            } catch (ArtifactNotFoundException ex) {
                                ex.printStackTrace();
    //                            Exceptions.printStackTrace(ex);
                            }
                        }
                        if (f.exists()) {
                            cpFiles.add(f);
                        }
                    }
                } catch (ComponentLookupException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return cpFiles;
    }

}
