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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.internal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.spi.AbstractQualitySession;
import org.nbheaven.sqe.codedefects.core.spi.SQECodedefectScanner;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleQualityProvider;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleResult;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.CheckstyleSession;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Sven Reimers
 */
@ProjectServiceProvider(service = {CheckstyleSession.class, QualitySession.class, ProjectOpenedHook.class},
        projectTypes = {
            @ProjectType(position = 30, id = "org-netbeans-modules-ant-freeform"),
            @ProjectType(position = 30, id = "org-netbeans-modules-autoproject"),
            @ProjectType(position = 30, id = "org-netbeans-modules-apisupport-project"),
            @ProjectType(position = 30, id = "org-netbeans-modules-java-j2seproject"),
//            @ProjectType(position = 30, id = "org-netbeans-modules-web-project"),
            @ProjectType(position = 30, id = "org-netbeans-modules-maven"),
            @ProjectType(position = 30, id = "org.netbeans.gradle.project")
        }
)
public class CheckstyleSessionImpl extends AbstractQualitySession<CheckstyleQualityProvider, CheckstyleResult> implements CheckstyleSession {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private CheckstyleResult checkstyleResult;

    /**
     * Creates a new instance of CheckstyleSession
     *
     * @param project the project this QualitySession belongs to.
     */
    public CheckstyleSessionImpl(Project project) {
        super(CheckstyleQualityProvider.getDefault(), project);
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
    public static CheckstyleResult computeResultAndWait(FileObject sourceFile) {
        Project project = SQEProjectSupport.findProjectByFileObject(sourceFile);
        CheckstyleScannerJob job = new CheckstyleFileScannerJob(project, sourceFile);
        SQECodedefectScanner.postAndWait(job);
        return job.getCheckstyleResult();
    }

    @Override
    public CheckstyleResult computeResultAndWait() {
        waitResultLock.lock();
        try {
            computeResult();
            while (isRunning.get()) {
                waitForResult.awaitUninterruptibly();
            }
            return this.checkstyleResult;
        } finally {
            waitResultLock.unlock();
        }
    }

    @Override
    public void computeResult() {
        if (!isRunning.getAndSet(true)) {
            CheckstyleScannerJob job = new CheckstyleProjectScannerJob(this);
            SQECodedefectScanner.post(job);
        } else {
//            System.out.println("Checkstyle is already running - Skip call to computeResult()");
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

    void setResultInternal(CheckstyleResult checkstyleResult) {
        setResult(checkstyleResult);
    }
}
