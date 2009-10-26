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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.nbheaven.sqe.core.maven.utils.FileUtilities;
import org.nbheaven.sqe.core.maven.utils.MavenUtilities;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDIncludes;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettings;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service = PMDSettingsProvider.class, projectType = "org-netbeans-modules-maven")
public class MavenPmdSettingsProvider implements PMDSettingsProvider {

    private final Project p;
    private PMDIncludes includes;

    public MavenPmdSettingsProvider(Project project) {
        this.p = project;
        includes = new MavenPMDIncludes(p);
    }

    public PMDSettings getPMDSettings() {
        final MavenPluginConfiguration pluginConfiguration = MavenUtilities.getReportPluginConfiguration(p, "org.apache.maven.plugins", "maven-pmd-plugin");
        if (pluginConfiguration.isDefinedInProject()) {
            RuleSetFactory rsf = new RuleSetFactory();
            final RuleSet toRet = new RuleSet();
            String[] ruleSets = pluginConfiguration.getStringListValue("rulesets", "ruleset");
            if (ruleSets == null) {
                ruleSets = MavenDefaults.DEFAULT_RULESETS;
            }
            List<File> deps = MavenUtilities.findDependencyArtifacts(p, "org.apache.maven.plugins", "maven-pmd-plugin", true);
            for (String setString : ruleSets) {
                 String nonLeadingSlash = setString.startsWith("/") ? setString.substring(1) : setString;
                //check is file is physically present in the project
                File file = FileUtilities.resolveFilePath(FileUtil.toFile(p.getProjectDirectory()), setString);
                FileObject fo = null;
                if (file == null || !file.exists()) {
                    //check the default configurations present in the maven-pmd-plugin
                    //check is file is present in the project's pmd plugin classpath
                    if (deps.size() > 0) {
                        for (File d : deps) {
                            FileObject fileFO = FileUtil.toFileObject(d);
                            if (FileUtil.isArchiveFile(fileFO)) {
                                FileObject root = FileUtil.getArchiveRoot(fileFO);
                                if (root != null) {
                                    fo = root.getFileObject(nonLeadingSlash);
                                    if (fo != null) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    fo = FileUtil.toFileObject(file);
                }
                if (fo != null) {
                    try {
                        toRet.addRuleSet(rsf.createRuleSet(fo.getInputStream()));
                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    try {
                        URL url = new URL(setString);
                        toRet.addRuleSet(rsf.createRuleSet(url.openStream()));
                    } catch (IOException ex) {
                        ex.printStackTrace();
//                            Exceptions.printStackTrace(ex);
                    }
                }
            }
            return new PMDSettings() {

                public void activateRule(Rule rule) {
                }

                public void deactivateRule(Rule rule) {
                }

                public boolean isRuleActive(Rule rule) {
                    return true;
                }

                public RuleSet getActiveRules() {
                        RuleSet activeRuleSet = new RuleSet();
                        //the default as defined in maven pmd plugin
                        int minimumPriority = MavenDefaults.DEFAULT_RULE_PRIORITY;
                        String priorityLevelString = pluginConfiguration.getValue("minimumPriority");
                        if (priorityLevelString != null) {
                            try {
                                minimumPriority = Integer.parseInt(priorityLevelString);
                            } catch (NumberFormatException e) {
                                //just swallow..
                            }
                        }

                        for (Rule rule : toRet.getRules()) {
                            if (rule.getPriority() <= minimumPriority) {
                                activeRuleSet.addRule(rule);
                            }
                        }

                        return activeRuleSet;

                }
            };
        }
        return null;
    }

    public PMDIncludes getPMDIncludes() {
        return includes;
    }
}
