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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.spi.AbstractQualitySession;
import org.nbheaven.sqe.codedefects.core.spi.SQECodedefectScanner;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 * 
 * @author Sven Reimers
 */
@ProjectServiceProvider(service={FindBugsSession.class, QualitySession.class}, projectType={
    "org-netbeans-modules-apisupport-project",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-maven"
}, projectTypes=@ProjectType(id="org-netbeans-modules-ant-freeform", position=0))
public class FindBugsSession extends AbstractQualitySession {

    private FindBugsResult findBugsResult;
    private AtomicBoolean isRunning;

    public FindBugsSession(Project project) {
        super(FindBugsQualityProvider.getDefault(), project);
        isRunning = new AtomicBoolean(false);
    }

    @Override
    public FindBugsQualityProvider getProvider() {
        return (FindBugsQualityProvider) super.getProvider();
    }

    public FindBugsResult getResult() {
        return findBugsResult;
    }

    private Lock waitResultLock = new ReentrantLock();
    private Condition waitForResult = waitResultLock.newCondition();

    public FindBugsResult computeResultAndWait(FileObject... fileObjects) {
        FindBugsScannerJob job = new FindBugsFileScannerJob(getProject(), fileObjects);
        SQECodedefectScanner.postAndWait(job);
        return job.getResult();
    }

    public FindBugsResult computeResultAndWait() {
        waitResultLock.lock();
        computeResult();
        waitForResult.awaitUninterruptibly();
        waitResultLock.unlock();
        return this.findBugsResult;
    }

    public void computeResult() {
        if (!isRunning.getAndSet(true)) {
            FindBugsScannerJob job = new FindBugsProjectScannerJob(this.getProject());
            SQECodedefectScanner.post(job);
        } else {
            //            System.out.println("Skip FindBugs...");
            }
    }

    void scanningDone() {
        waitResultLock.lock();
        isRunning.set(false);
        waitForResult.signalAll();
        waitResultLock.unlock();
    }

    void setResult(FindBugsResult findBugsResult) {
        FindBugsResult oldResult = this.findBugsResult;
        this.findBugsResult = findBugsResult;
        resultChanged(oldResult, this.findBugsResult);
    }

    void resultChanged(FindBugsResult oldResult, FindBugsResult newResult) {
        fireResultChanged(oldResult, newResult);
    }
}
