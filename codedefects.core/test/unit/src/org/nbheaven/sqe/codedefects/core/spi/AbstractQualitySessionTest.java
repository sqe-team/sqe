/* Copyright 2010 Jesse Glick.
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
 * along with SQE.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nbheaven.sqe.codedefects.core.spi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.Icon;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

public class AbstractQualitySessionTest extends NbTestCase {

    public AbstractQualitySessionTest(String name) {
        super(name);
    }
    
    public void testSQE49Leak() throws Exception {
        Project p = new Project() {
            @Override
            public FileObject getProjectDirectory() {
                return null;
            }
            @Override
            public Lookup getLookup() {
                return null;
            }
        };
        QualityProvider qp = new QualityProvider() {
            @Override
            public Class<? extends QualitySession> getQualitySessionClass() {
                return QualitySession.class;
            }
            @Override
            public QualitySession createQualitySession(Project project) {
                return null;
            }
            @Override
            public String getDisplayName() {
                return "Test";
            }
            @Override
            public String getId() {
                return "test";
            }
            @Override
            public Icon getIcon() {
                return null;
            }
            @Override
            public boolean isValidFor(Project project) {
                return true;
            }
            @Override
            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
        new AbstractQualitySession(qp, p) {
            @Override
            public QualityResult getResult() {
                return null;
            }
            @Override
            public void computeResult() {}
        };
        qp = null;
        Reference<?> ref = new WeakReference<Object>(p);
        p = null;
        assertGC("can collect project reference", ref);
    }

}