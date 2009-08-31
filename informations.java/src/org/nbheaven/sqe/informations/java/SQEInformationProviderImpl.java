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
package org.nbheaven.sqe.informations.java;

import java.lang.ref.WeakReference;
import org.nbheaven.sqe.informations.ui.spi.SQEInformationComponent;
import org.nbheaven.sqe.informations.ui.spi.SQEInformationProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;


/**
 *
 * @author Sven Reimers
 */
@ProjectServiceProvider(service=SQEInformationProvider.class, projectType={
    "org-netbeans-modules-ant-freeform",
    "org-netbeans-modules-apisupport-project",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"
})
public class SQEInformationProviderImpl implements SQEInformationProvider {
    
    private WeakReference<SQEInformationComponent> component = null;
    final Project project;
    
    public SQEInformationProviderImpl(Project project) {
        this.project = project;
    }

    public synchronized SQEInformationComponent getInformationComponent() {
        SQEInformationComponent info = null;
        if (null == component) {
            info = new SQEInformationComponentImpl(project);
            component = new WeakReference<SQEInformationComponent>(info);
        } else {
            info = component.get();
            if (null == info) {
                info = new SQEInformationComponentImpl(project);
                component = new WeakReference<SQEInformationComponent>(info);
            }
        }        
        return info;
    }
    
    
}
