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
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectBuildingResult;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
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

            public String[] getStringListValue(String listParent, String listChild) {
                //TODO does it contain report plugin config most probably not?
                return getReportPluginPropertyList(project, groupId, artifactId, listParent, listChild, null);
            }
        };
    }

//    public static boolean definesReportPlugin(Project prj, String groupId, String artifactId) {
//
//    }

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
                        try {
                            MavenProjectBuilder mpb = (MavenProjectBuilder) online.getPlexusContainer().lookup(MavenProjectBuilder.class);
                            DefaultProjectBuilderConfiguration dpbc = new DefaultProjectBuilderConfiguration();
                            dpbc.setLocalRepository(online.getLocalRepository());
                            MavenProject mp = mpb.buildFromRepository(a, p.getMavenProject().getRemoteArtifactRepositories(), online.getLocalRepository());
                            if (mp != null) {
                                System.out.println("mp=" + mp.getFile());
                                System.out.println("art=" + mp.getArtifact());
                                File pom = new File(f.getParentFile(), f.getName().replace(".jar", ".pom"));
                                System.out.println("pom=" + pom);
                                MavenProjectBuildingResult res = mpb.buildProjectWithDependencies(pom, dpbc);
                                mp = res.getProject();

                                Set<Artifact> depArts = mp.getDependencyArtifacts();
                                for (Artifact depA : depArts) {
                                    File df = FileUtil.normalizeFile(new File(new File(online.getLocalRepository().getBasedir()), online.getLocalRepository().pathOf(depA)));
                                    System.out.println("depfile=" + df);
                                    if (df.exists()) {
                                        cpFiles.add(df);
                                    }
                                }
                            }
                        } catch (ProjectBuildingException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (ComponentLookupException ex) {
                            Exceptions.printStackTrace(ex);
                        }
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


//-------------------------------------------------------------------------------
// start: this part is to be deleted once upgrading to 6.8 nb
//-------------------------------------------------------------------------------

        /**
     * gets the list of values for the given property, if configured in the current project.
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    private static String[] getReportPluginPropertyList(Project prj, String groupId, String artifactId, String multiproperty, String singleproperty, String goal) {
        NbMavenProject project = prj.getLookup().lookup(NbMavenProject.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getReportPluginPropertyList(project.getMavenProject(), groupId, artifactId, multiproperty, singleproperty, goal);
    }

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
