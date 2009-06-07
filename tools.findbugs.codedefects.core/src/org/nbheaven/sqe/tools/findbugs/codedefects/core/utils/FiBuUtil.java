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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.utils;

import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;

import java.util.Iterator;


/**
 *
 * @author sven
 */
public final class FiBuUtil {
    /** Creates a new instance of FiBuUtil */
    private FiBuUtil() {
    }

    public static boolean isBugPatternIssuedFromCore(BugPattern bugPattern) {
        Plugin plugin = DetectorFactoryCollection.instance()
                                                 .getPluginById("edu.umd.cs.findbugs.plugins.core");

        for (Iterator<BugPattern> bugIt = plugin.bugPatternIterator();
                bugIt.hasNext();) {
            if (bugPattern.equals(bugIt.next())) {
                return true;
            }
        }

        return false;
    }
}
