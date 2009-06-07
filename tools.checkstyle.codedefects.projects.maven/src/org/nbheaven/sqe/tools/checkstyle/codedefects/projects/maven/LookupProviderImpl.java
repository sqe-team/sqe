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
package org.nbheaven.sqe.tools.checkstyle.codedefects.projects.maven;

import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleQualityProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sven
 */
public class LookupProviderImpl implements LookupProvider{
    
    /** Creates a new instance of LookupProviderImpl */
    public LookupProviderImpl() {
    }
    
    public Lookup createAdditionalLookup(Lookup context) {
        Project p = context.lookup(Project.class);

        return Lookups.fixed(new Object[]{CheckstyleQualityProvider.getDefault().createQualitySession(p), new CheckstyleSettingsProviderImpl(p)});
    }

}
