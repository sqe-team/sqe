/* Copyright 2009, Jesse Glick.
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
 * along with SQE.  If not, see <http://www.gnu.org/licenses/>.
 */

@OptionsPanelController.ContainerRegistration(
    id=OPTIONS_CATEGORY_ID,
    categoryName="#OptionsCategory_Name",
    iconBase="org/nbheaven/sqe/core/ui/resources/sqe_32.png",
    position=350
)
@Registrations({
    @Registration(projectType="org-netbeans-modules-ant-freeform", category=CUSTOMIZER_CATEGORY_ID, categoryLabel="#customizer_category_name", position=650),
    @Registration(projectType="org-netbeans-modules-apisupport-project", category=CUSTOMIZER_CATEGORY_ID, categoryLabel="#customizer_category_name", position=450),
    @Registration(projectType="org-netbeans-modules-java-j2seproject", category=CUSTOMIZER_CATEGORY_ID, categoryLabel="#customizer_category_name", position=250),
    @Registration(projectType="org-netbeans-modules-web-project", category=CUSTOMIZER_CATEGORY_ID, categoryLabel="#customizer_category_name", position=350),
    @Registration(projectType="org-netbeans-modules-maven", category=CUSTOMIZER_CATEGORY_ID, categoryLabel="#customizer_category_name", position=650)
})
package org.nbheaven.sqe.core.ui;

import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registrations;
import static org.nbheaven.sqe.core.ui.Constants.*;
