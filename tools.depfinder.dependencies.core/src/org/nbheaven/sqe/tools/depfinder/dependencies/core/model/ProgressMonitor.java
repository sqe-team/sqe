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
package org.nbheaven.sqe.tools.depfinder.dependencies.core.model;

import com.jeantessier.classreader.ClassfileScanner;
import com.jeantessier.classreader.LoadEvent;
import com.jeantessier.dependencyfinder.VerboseListenerBase;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 *
 * @author fvo
 */
public class ProgressMonitor extends VerboseListenerBase {

    private final String projectName;
    private final AtomicInteger doneWorkUnits;
    private ProgressHandle prgHdl = null;

    public ProgressMonitor(final String projectName) {
        this.projectName = projectName;
        this.doneWorkUnits = new AtomicInteger(0);
    }

    public synchronized void startMonitoring(Collection<String> filenames) {
        if (null == prgHdl) {
            prgHdl = createProgressHandle();
        }
        prgHdl.start();

        prgHdl.progress("Scanning ...");

        ClassfileScanner scanner = new ClassfileScanner();
        scanner.load(filenames);

        getProgressHandle().switchToDeterminate(scanner.getNbFiles());
        doneWorkUnits.set(0);
    }

    public synchronized void stopMonitoring() {
        if (prgHdl != null) {
            prgHdl.finish();
            prgHdl = null;
            doneWorkUnits.set(0);
        }
    }

    private ProgressHandle createProgressHandle() {
        return ProgressHandleFactory.createHandle("DependencyFinder - " + projectName);
    }

    public ProgressHandle getProgressHandle() {
        if (null == prgHdl) {
            prgHdl = createProgressHandle();
        }
        return prgHdl;
    }

    public void setProgressHandle(final ProgressHandle prgHdl) {
        this.prgHdl = prgHdl;
    }

    @Override
    public void beginSession(LoadEvent event) {
        super.beginSession(event);
        getProgressHandle().progress("Searching for classes ...");
    }

    @Override
    public void beginGroup(LoadEvent event) {
        super.beginGroup(event);
        getProgressHandle().progress("Loading from " + event.getGroupName() + " ...");
    }

    @Override
    public void beginFile(LoadEvent event) {
        super.beginFile(event);

        if (event.getFilename().startsWith(event.getGroupName())) {
            getProgressHandle().progress("Found " + event.getFilename() + " ...");

        } else {
            getProgressHandle().progress("Found " + event.getGroupName() + " >> " + event.getFilename() + " ...");
        }
    }

    @Override
    public void endFile(LoadEvent event) {
        super.endFile(event);
        getProgressHandle().progress(doneWorkUnits.incrementAndGet());
    }

    @Override
    public void endSession(LoadEvent event) {
        super.endSession(event);
        stopMonitoring();
    }
}
