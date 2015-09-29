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

import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.tools.pmd.codedefects.core.annotations.RuleViolationAnnotationProcessor;
import org.nbheaven.sqe.codedefects.core.spi.AbstractQualityProvider;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.nbheaven.sqe.tools.pmd.codedefects.core.ui.PMDTopComponent;
import org.openide.util.ImageUtilities;
import org.nbheaven.sqe.codedefects.ui.UIHandle;

/**
 *
 * @author Sven Reimers
 */
public final class PMDQualityProvider extends AbstractQualityProvider {

    private static interface Singleton {

        PMDQualityProvider INSTANCE = new PMDQualityProvider();
    }

    private final Lookup lookup;

    /**
     * Creates a new instance of PMDQualityProvider
     */
    private PMDQualityProvider() {
        lookup = Lookups.fixed(new Object[]{UIHandleImpl.INSTANCE, RuleViolationAnnotationProcessor.INSTANCE});
    }

    public static PMDQualityProvider getDefault() {
        return Singleton.INSTANCE;
    }

    @Override
    public PMDSession createQualitySession(Project project) {
        return new PMDSession(project);
    }

    @Override
    public String getDisplayName() {
        return "PMD";
    }

    @Override
    public String getId() {
        return "PMD";
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage(
                "org/nbheaven/sqe/tools/pmd/codedefects/core/resources/pmd.png"));
    }

    @Override
    public Class<? extends QualitySession> getQualitySessionClass() {
        return PMDSession.class;
    }

    private static class UIHandleImpl implements UIHandle {

        private static final UIHandle INSTANCE = new UIHandleImpl();

        @Override
        public void open() {
            PMDTopComponent tc = PMDTopComponent.findInstance();
            tc.open();
        }
    }
}
