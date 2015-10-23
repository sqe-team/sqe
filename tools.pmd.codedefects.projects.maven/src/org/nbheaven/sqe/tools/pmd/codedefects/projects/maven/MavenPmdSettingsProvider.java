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
import java.util.List;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSetReferenceId;
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
import org.openide.util.Lookup;

/**
 *
 * @author Martin Klähn
 */
@ProjectServiceProvider(service = PMDSettingsProvider.class, projectType = "org-netbeans-modules-maven")
public class MavenPmdSettingsProvider implements PMDSettingsProvider {

    private final Project p;
    private PMDIncludes includes;

    public MavenPmdSettingsProvider(Project project) {
        this.p = project;
        includes = new MavenPMDIncludes(p);
    }

    @Override
    public PMDSettings getPMDSettings() {
        final MavenPluginConfiguration pluginConfiguration = MavenUtilities.getReportPluginConfiguration(p, "org.apache.maven.plugins", "maven-pmd-plugin");
        if (pluginConfiguration.isDefinedInProject()) {
            RuleSetFactory rsf = new RuleSetFactory();
            rsf.setClassLoader(Lookup.getDefault().lookup(ClassLoader.class));
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
                        RuleSetReferenceId id = new RuleSetReferenceId(fo.getPath());
                        toRet.addRuleSet(rsf.createRuleSet(id));
                    } catch (RuleSetNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (!setString.startsWith("/")) {
                    try {
                        RuleSetReferenceId id = new RuleSetReferenceId(setString);
                        toRet.addRuleSet(rsf.createRuleSet(id));
                    } catch (RuleSetNotFoundException ex) {
//                        ex.printStackTrace();
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return new PMDSettings() {

                @Override
                public void activateRule(Rule rule) {
                }

                @Override
                public void deactivateRule(Rule rule) {
                }

                @Override
                public boolean isRuleActive(Rule rule) {
                    return true;
                }

                @Override
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
                        if (rule.getPriority().compareTo(RulePriority.valueOf(minimumPriority)) <= 0) {
                            activeRuleSet.addRule(rule);
                        } else {
                        }
                    }

                    return activeRuleSet;

                }
            };
        }
        return null;
    }

    @Override
    public PMDIncludes getPMDIncludes() {
        return includes;
    }
}
