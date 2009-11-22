/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbheaven.sqe.core.maven.utils;

import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.openide.util.Lookup;

/**
 * A proxy of ModelHandle class for use in customizers in projects
 * that don't have a friend dependency to maven module
 * @author mkleint
 */
public class ModelHandleProxy {
    private final ModelHandle handle;

    private ModelHandleProxy(ModelHandle hand, Project prj) {
        handle = hand;
    }

    public static ModelHandleProxy create(Lookup customizerLookup) {
        ModelHandle h = customizerLookup.lookup(ModelHandle.class);
        Project p = customizerLookup.lookup(Project.class);
        assert h != null;
        return new ModelHandleProxy(h, p);
    }

    public POMModel getPOMModel() {
        return handle.getPOMModel();
    }

    public ProfilesModel getProfilesModel() {
        return handle.getProfileModel();
    }

    public void markAsModified(POMModel model) {
        handle.markAsModified(model);
    }

    public void markAsModified(ProfilesModel model) {
        handle.markAsModified(model);
    }

    public MavenPluginConfiguration getMavenPluginConfig(String groupId, String artifactId) {
        return MavenUtilities.getReportPluginConfigurationImpl(handle.getProject(), groupId, artifactId);
    }

}
