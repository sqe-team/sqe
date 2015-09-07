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
package org.nbheaven.sqe.tools.pmd.codedefects.core;

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
@ProjectServiceProvider(service = {PMDSession.class, QualitySession.class},
        projectTypes = {
            @ProjectType(position = 20, id = "org-netbeans-modules-ant-freeform"),
            @ProjectType(position = 20, id = "org-netbeans-modules-autoproject"),
            @ProjectType(position = 20, id = "org-netbeans-modules-apisupport-project"),
            @ProjectType(position = 20, id = "org-netbeans-modules-java-j2seproject"),
            @ProjectType(position = 20, id = "org-netbeans-modules-web-project"),
            @ProjectType(position = 20, id = "org-netbeans-modules-maven"),
			@ProjectType(position = 20, id = "org.netbeans.gradle.project")
        }
)
public class PMDSession extends AbstractQualitySession {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private PMDResult pmdResult;

    /**
     * Creates a new instance of FindBugsSession
     *
     * @param project the project this QualitySession belongs to.
     */
    public PMDSession(Project project) {
        super(PMDQualityProvider.getDefault(), project);
    }

    @Override
    public PMDQualityProvider getProvider() {
        return (PMDQualityProvider) super.getProvider();
    }

    @Override
    public PMDResult getResult() {
        return pmdResult;
    }

    private final Lock waitResultLock = new ReentrantLock();
    private final Condition waitForResult = waitResultLock.newCondition();

    /**
     * Analyze a single file. Call within a Java source task at
     * {@link org.netbeans.api.java.source.JavaSource.Phase#UP_TO_DATE}.
     *
     * @param sourceFile The file to analyze
     * @return the result of the analyzation
     */
    public PMDResult computeResultAndWait(FileObject sourceFile) {
        PMDScannerJob job = new PMDFileScannerJob(getProject(), sourceFile);
        SQECodedefectScanner.postAndWait(job);
        return job.getPMDResult();
    }

    public PMDResult computeResultAndWait() {
        waitResultLock.lock();
        try {
            computeResult();
            while (isRunning.get()) {
                waitForResult.awaitUninterruptibly();
            }
            return this.pmdResult;
        } finally {
            waitResultLock.unlock();
        }
    }

    @Override
    public void computeResult() {
        if (!isRunning.getAndSet(true)) {
            PMDScannerJob job = new PMDProjectScannerJob(this);
            SQECodedefectScanner.post(job);
        } else {
//            System.out.println("PMD is already running - Skip call to computeResult()");
        }
    }

    void scanningDone() {
        waitResultLock.lock();
        try {
            isRunning.set(false);
            waitForResult.signalAll();
        } finally {
            waitResultLock.unlock();
        }
    }

    void setResult(PMDResult pmdResult) {
        PMDResult oldResult = this.pmdResult;
        this.pmdResult = pmdResult;
        fireResultChanged(oldResult, this.pmdResult);
    }
}