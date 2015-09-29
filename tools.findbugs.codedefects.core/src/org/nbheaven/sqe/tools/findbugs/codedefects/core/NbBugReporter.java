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

import edu.umd.cs.findbugs.AnalysisError;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.TextUIBugReporter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Reimers
 */
final class NbBugReporter extends TextUIBugReporter {

    private static final Logger LOG = Logger.getLogger(NbBugReporter.class.getName());

    private final FindBugsResult findbugsResult;
    private final NbFindBugsProgress progressCallback;
    private final SortedBugCollection bugCollection = new SortedBugCollection();

    NbBugReporter(FindBugsResult findbugsResult, NbFindBugsProgress progressCallback) {
        this.progressCallback = progressCallback;
        this.findbugsResult = findbugsResult;
    }

    @Override
    protected void doReportBug(edu.umd.cs.findbugs.BugInstance bugInstance) {
        findbugsResult.add(bugInstance);
    }

    @Override
    public BugCollection getBugCollection() {
        System.err.println("Call to getBugCollection");
        return bugCollection;
    }

    @Override
    public void finish() {
    }

    public void observeClass(org.apache.bcel.classfile.JavaClass javaClass) {
        progressCallback.getProgressHandle().progress("Scanning " + javaClass.getClassName());
    }

    @Override
    public void observeClass(edu.umd.cs.findbugs.classfile.ClassDescriptor classDescriptor) {
        progressCallback.getProgressHandle().progress("Scanning " + classDescriptor.getClassName());
    }

    @Override
    public void reportAnalysisError(AnalysisError error) {
        LOG.log(Level.INFO, error.getMessage(), error.getException());
    }

    @Override
    public void reportMissingClass(String message) {
        /* XXX printed for javax.annotation.NonNull on every run; not obviously helpful:
        StatusDisplayer.getDefault().setStatusText("Missing class: " + message);
         */
        LOG.log(Level.FINE, "Missing class: {0}", message);
    }

}
