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
import java.util.Properties;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.nbheaven.sqe.core.maven.spi.MavenPluginConfigurationImpl;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;

/**
 *
 * @author sven
 */
public final class MavenUtilities {

    private MavenUtilities() {}

    /** deprecated */
    @Deprecated
    public static MavenPluginConfiguration getPluginConfiguration(Project project, String groupId, String artifactId) {
        MavenProject mavenProject = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        Xpp3Dom reportConfiguration = mavenProject.getReportConfiguration(groupId, artifactId, null);
        if (null != reportConfiguration) {
            ExpressionEvaluator eval = new NBPluginParameterExpressionEvaluator(mavenProject,
                    MavenSettingsSingleton.getInstance().createUserSettingsModel(),
                    new Properties());

            return new MavenPluginConfigurationImpl(reportConfiguration, eval);
        }
        return null;
    }

    public static MavenPluginConfiguration getReportPluginConfiguration(final Project project, final String groupId, final String artifactId) {
        return new MavenPluginConfiguration() {
            public String getValue(String path) {
                return PluginPropertyUtils.getReportPluginProperty(project, groupId, artifactId, path, null);
            }
        };
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
        if (includePluginArtifact) {
            //TODO check also getReportArtifacts()?
            Set<Artifact> arts = new HashSet<Artifact>();
            arts.addAll(NbCollections.checkedSetByFilter(p.getMavenProject().getReportArtifacts(), Artifact.class, true));
            arts.addAll(NbCollections.checkedSetByFilter(p.getMavenProject().getPluginArtifacts(), Artifact.class, true));
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
                            online.resolve(a, p.getMavenProject().getRemoteArtifactRepositories(), online.getLocalRepository());
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
            }
            
        }
        //TODO only build or also getReportPlugins()?
        List<Plugin> plugins = NbCollections.checkedListByCopy(p.getMavenProject().getBuildPlugins(), Plugin.class, true);
        for (Plugin plug : plugins) {
            if (pluginArtifactId.equals(plug.getArtifactId()) &&
                    pluginGroupId.equals(plug.getGroupId())) {
                try {
                    List<Dependency> deps = NbCollections.checkedListByCopy(plug.getDependencies(), Dependency.class, true);
                    ArtifactFactory artifactFactory = (ArtifactFactory) online.getPlexusContainer().lookup(ArtifactFactory.class);
                    for (Dependency d : deps) {
                        final Artifact projectArtifact = artifactFactory.createArtifactWithClassifier(d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getType(), d.getClassifier());
                        String localPath = online.getLocalRepository().pathOf(projectArtifact);
                        File f = FileUtil.normalizeFile(new File(online.getLocalRepository().getBasedir(), localPath));
                        if (!f.exists()) {
                            try {
                                online.resolve(projectArtifact, p.getMavenProject().getRemoteArtifactRepositories(), online.getLocalRepository());
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
