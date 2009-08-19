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
package org.nbheaven.sqe.tools.findbugs.codedefects.core;

import edu.umd.cs.findbugs.TextUIBugReporter;

/**
 *
 * @author sven
 */
final class NbBugReporter extends TextUIBugReporter {

    private FindBugsResult findbugsResult;
    private NbFindBugsProgress progressCallback;

    NbBugReporter(FindBugsResult findbugsResult,
            NbFindBugsProgress progressCallback) {
        this.progressCallback = progressCallback;
        this.findbugsResult = findbugsResult;
    }

    protected void doReportBug(edu.umd.cs.findbugs.BugInstance bugInstance) {
        findbugsResult.add(bugInstance);

        StringBuilder builder = new StringBuilder();

        if (null != bugInstance.getPrimaryClass()) {
            builder.append(bugInstance.getPrimaryClass().getClassName() + " ");
        }

        if (null != bugInstance.getPrimarySourceLineAnnotation()) {
            builder.append("[" +
                    bugInstance.getPrimarySourceLineAnnotation().getStartLine() +
                    "]: ");
        } else {
            if ((null != bugInstance.getPrimaryMethod()) &&
                    (null != bugInstance.getPrimaryMethod().getSourceLines())) {
                builder.append("[" +
                        (bugInstance.getPrimaryMethod().getSourceLines().getStartLine() - 1) + "]: ");
            } else if (null != bugInstance.getPrimaryField()) {
                builder.append("[Field: " +
                        bugInstance.getPrimaryField().getFieldName() + "]: ");
            } else {
                builder.append("[Class]: ");
            }
        }

        if (null != bugInstance.getMessage()) {
            builder.append(bugInstance.getMessage());
        }
    }

    public void finish() {
    }

    public void observeClass(org.apache.bcel.classfile.JavaClass javaClass) {
        progressCallback.getProgressHandle().progress("Scanning " + javaClass.getClassName());
    }

    public void observeClass(
            edu.umd.cs.findbugs.classfile.ClassDescriptor classDescriptor) {
        progressCallback.getProgressHandle().progress("Scanning " + classDescriptor.getClassName());
    }
}
