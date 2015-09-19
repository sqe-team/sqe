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

package org.nbheaven.sqe.tools.findbugs.codedefects.projects.maven;

import javax.swing.JComponent;
import org.nbheaven.sqe.core.maven.utils.ModelHandleProxy;
import org.nbheaven.sqe.core.ui.Constants;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
@CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-maven", category=Constants.CUSTOMIZER_CATEGORY_ID)
public class FindbugsPanelProvider implements CompositeCategoryProvider {

    @Override
    public Category createCategory(Lookup lkp) {
        ModelHandleProxy proxy = ModelHandleProxy.create(lkp);
        assert proxy != null;
        return Category.create("Findbugs", "Findbugs", null);
    }

    @Override
    public JComponent createComponent(Category ctgr, Lookup lkp) {
        ModelHandleProxy proxy = ModelHandleProxy.create(lkp);
        return new FindbugsPanel(proxy);
    }

}
