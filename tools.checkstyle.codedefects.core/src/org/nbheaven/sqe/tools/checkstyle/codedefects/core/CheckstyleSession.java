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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.spi.AbstractQualitySession;
import org.nbheaven.sqe.codedefects.core.spi.SQECodedefectScanner;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Sven Reimers
 */
@ProjectServiceProvider(service={CheckstyleSession.class, QualitySession.class}, projectType={
    "org-netbeans-modules-ant-freeform",
    "org-netbeans-modules-apisupport-project",
    "org-netbeans-modules-java-j2seproject",
//    "org-netbeans-modules-web-project",
    "org-netbeans-modules-maven"
})
public class CheckstyleSession extends AbstractQualitySession {

    private CheckstyleResult checkstyleResult;
    private AtomicBoolean isRunning;

    /**
     * Creates a new instance of CheckstyleSession
     */
    public CheckstyleSession(Project project) {
        super(CheckstyleQualityProvider.getDefault(), project);
        isRunning = new AtomicBoolean(false);
    }

    @Override
    public CheckstyleQualityProvider getProvider() {
        return (CheckstyleQualityProvider) super.getProvider();
    }

    public CheckstyleResult getResult() {
        return checkstyleResult;
    }
    private Lock waitResultLock = new ReentrantLock();
    private Condition waitForResult = waitResultLock.newCondition();

    public CheckstyleResult computeResultAndWait(FileObject... fileObjects) {
        CheckstyleScannerJob job = new CheckstyleFileScannerJob(getProject(), fileObjects);
        SQECodedefectScanner.postAndWait(job);
        return job.getCheckstyleResult();
    }

    public CheckstyleResult computeResultAndWait() {
        waitResultLock.lock();
        computeResult();
        waitForResult.awaitUninterruptibly();
        waitResultLock.unlock();
        return this.checkstyleResult;
    }

    public void computeResult() {
        if (!isRunning.getAndSet(true)) {
            CheckstyleScannerJob job = new CheckstyleProjectScannerJob(this);
            SQECodedefectScanner.post(job);
        } else {
//            System.out.println("Skip Checkstyle...");
        }
    }

    void scanningDone() {
        waitResultLock.lock();
        isRunning.set(false);
        waitForResult.signalAll();
        waitResultLock.unlock();
    }

    void setResult(CheckstyleResult checkstyleResult) {
        CheckstyleResult oldResult = this.checkstyleResult;
        this.checkstyleResult = checkstyleResult;
        fireResultChanged(oldResult, this.checkstyleResult);
    }
}