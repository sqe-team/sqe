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
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator;
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
            public String getValue(String path) {
                return PluginPropertyUtils.getReportPluginProperty(project, groupId, artifactId, path, null);
            }

            public String[] getStringListValue(String listParent, String listChild) {
                //TODO does it contain report plugin config most probably not?
                return getReportPluginPropertyList(project, groupId, artifactId, listParent, listChild, null);
            }

            public boolean isDefinedInProject() {
                return definesReportPlugin(project, groupId, artifactId);
            }

        };
    }


    static boolean definesReportPlugin(MavenProject mp, String groupId, String artifactId) {
        @SuppressWarnings("unchecked")
        List<ReportPlugin> rps = mp.getReportPlugins();
        for (ReportPlugin rp : rps) {
            if (groupId.equals(rp.getGroupId()) && artifactId.equals(rp.getArtifactId())) {
                return true;
            }
        }
        if (mp.getPluginManagement() != null) {
            for (Object obj : mp.getPluginManagement().getPlugins()) {
                Plugin plug = (Plugin)obj;
                if (groupId.equals(plug.getGroupId()) &&
                    artifactId.equals(plug.getArtifactId())) {
                    return true;
                }
            }
        }
        return false;
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


//-------------------------------------------------------------------------------
// start: this part is to be deleted once upgrading to 6.8 nb
// (XXX but replaced with what?)
//-------------------------------------------------------------------------------

    /**
     * gets the list of values for the given property, if configured in the current project.
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    private static String[] getReportPluginPropertyList(MavenProject prj, String groupId, String artifactId, String multiproperty, String singleproperty, String goal) {
        String[] toRet = null;
        if (prj.getReportPlugins() == null) {
            return toRet;
        }
        for (Object obj : prj.getReportPlugins()) {
            ReportPlugin plug = (ReportPlugin)obj;
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                if (plug.getReportSets() != null) {
                    for (Object obj2 : plug.getReportSets()) {
                        ReportSet exe = (ReportSet)obj2;
                        if (exe.getReports().contains(goal)) {
                            toRet = checkListConfiguration(prj, exe.getConfiguration(), multiproperty, singleproperty);
                            if (toRet != null) {
                                break;
                            }
                        }
                    }
                }
                if (toRet == null) {
                    toRet = checkListConfiguration(prj, plug.getConfiguration(), multiproperty, singleproperty);
                }
            }
        }
        if (toRet == null) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Object obj : prj.getPluginManagement().getPlugins()) {
                    Plugin plug = (Plugin)obj;
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = checkListConfiguration(prj, plug.getConfiguration(), multiproperty, singleproperty);
                        break;
                    }
                }
            }
        }
        return toRet;
    }


    private static String[] checkListConfiguration(MavenProject prj, Object conf, String multiproperty, String singleproperty) {
        if (conf != null && conf instanceof Xpp3Dom) {
            Xpp3Dom dom = (Xpp3Dom)conf;
            Xpp3Dom source = dom.getChild(multiproperty);
            if (source != null) {
                List<String> toRet = new ArrayList<String>();
                Xpp3Dom[] childs = source.getChildren(singleproperty);
                NBPluginParameterExpressionEvaluator eval = new NBPluginParameterExpressionEvaluator(prj, EmbedderFactory.getProjectEmbedder().getSettings(), new Properties());
                for (Xpp3Dom ch : childs) {
                    try {
                        Object evaluated = eval.evaluate(ch.getValue().trim());
                        toRet.add(evaluated != null ? ("" + evaluated) : ch.getValue().trim());  //NOI18N
                    } catch (ExpressionEvaluationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return toRet.toArray(new String[toRet.size()]);
            }
        }
        return null;
    }
//-------------------------------------------------------------------------------
// end: this part is to be deleted once upgrading to 6.8 nb
//-------------------------------------------------------------------------------


}
