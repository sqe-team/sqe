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

import java.util.Collection;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDIncludes;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.PMDSettingsProvider;
import org.nbheaven.sqe.tools.pmd.codedefects.core.settings.impl.DefaultPMDIncludes;
import org.openide.filesystems.FileObject;

/**
 *
 * @author fvo
 */
final class PMDProjectScannerJob extends PMDScannerJob {

    private PMDSession session;

    public PMDProjectScannerJob(PMDSession session) {
        super(session.getProject());
        this.session = session;
    }

    protected void executePMD() {
        PMDSettingsProvider prv = getProject().getLookup().lookup(PMDSettingsProvider.class);
        Collection<FileObject> includes = null;
        if (prv != null) {
            PMDIncludes inc = prv.getPMDIncludes();
            if (inc != null) {
                includes = inc.getProjectIncludes();
            }
        }
        if (includes == null) {
            //default behaviour..
            includes = new DefaultPMDIncludes(getProject()).getProjectIncludes();
        }
        executePMD(includes);

    }

    @Override
    protected final void postScan() {
        session.setResult(getPMDResult());
        super.postScan();
        session.scanningDone();
    }
}
