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

import edu.umd.cs.findbugs.FindBugsProgress;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.Cancellable;

class NbFindBugsProgress implements FindBugsProgress {

    private final ProgressHandle progressHandle;
    private Project p;
    private int analyzed = 0;
    private int analyzePass = 0;
    private int[] classesPerPass = null;

    NbFindBugsProgress(Project p, ProgressHandle progressHandle) {
        this.p = p;
        this.progressHandle = progressHandle;
    }

    public void reportNumberOfArchives(final int numArchives) {
        setProgressHandleDisplayName("Scanning Archives");
    }

    public void startArchive(String archiveName) {
        setProgressHandleDisplayName("Scanning Archive:" + archiveName);;
    }

    public void finishArchive() {
    }

    public void startAnalysis(int numClasses) {
        analyzed = 0;
        setProgressHandleDisplayName("Analyzing Classes - Pass " + analyzePass);
        getProgressHandle().switchToDeterminate(numClasses);
    }

    public void finishClass() {
        getProgressHandle().progress(++analyzed);
    }

    public void finishPerClassAnalysis() {
        getProgressHandle().switchToIndeterminate();
        ++analyzePass;
    }

    public ProgressHandle getProgressHandle() {
        return progressHandle;
    }

    public void predictPassCount(int[] classesPerPass) {
        this.classesPerPass = classesPerPass;
    }

    private void setProgressHandleDisplayName(java.lang.String stage) {
        getProgressHandle().setDisplayName("FindBugs " + ProjectUtils.getInformation(p).getDisplayName() +
                " - [" + stage + "] ");
    }

    private static class CancelCallback implements Cancellable {

        private boolean cancelled = false;

        public CancelCallback() {
        }

        public boolean cancel() {
            cancelled = true;
            return true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }
}
