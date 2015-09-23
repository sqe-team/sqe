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

/**
 *
 * @author Martin Klähn
 */
public class MavenDefaults {

    static final int DEFAULT_RULE_PRIORITY = 5;
    static final String[] DEFAULT_RULESETS =  new String[] {
                  "/rulesets/basic.xml",
                  "/rulesets/imports.xml",
                  "/rulesets/unusedcode.xml"
                };

}
