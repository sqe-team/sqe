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

import org.nbheaven.sqe.tools.findbugs.codedefects.core.annotations.BugAnnotationProcessor;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.spi.AbstractQualityProvider;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.ui.FindBugsTopComponent;
import org.openide.util.ImageUtilities;
import org.nbheaven.sqe.codedefects.ui.UIHandle;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.internal.FindBugsSessionImpl;

/**
 *
 * @author Sven Reimers
 */
public final class FindBugsQualityProvider extends AbstractQualityProvider {

    private static interface Singleton {

        FindBugsQualityProvider INSTANCE = new FindBugsQualityProvider();
    }

    private final Lookup lookup;

    /**
     * Creates a new instance of FindBugsQualityProvider
     */
    private FindBugsQualityProvider() {
        lookup = Lookups.fixed(new Object[]{UIHandleImpl.INSTANCE, BugAnnotationProcessor.INSTANCE});
    }

    public static FindBugsQualityProvider getDefault() {
        return Singleton.INSTANCE;
    }

    @Override
    public FindBugsSession createQualitySession(Project project) {
        return new FindBugsSessionImpl(project);
    }

    @Override
    public String getDisplayName() {
        return "Find Bugs";
    }

    @Override
    public String getId() {
        return "FindBugs";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage("org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/bug.png"));
    }

    @Override
    public Class<? extends QualitySession> getQualitySessionClass() {
        return FindBugsSession.class;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private static class UIHandleImpl implements UIHandle {

        private static final UIHandle INSTANCE = new UIHandleImpl();

        @Override
        public void open() {
            FindBugsTopComponent tc = FindBugsTopComponent.findInstance();
            tc.open();
        }
    }
}
