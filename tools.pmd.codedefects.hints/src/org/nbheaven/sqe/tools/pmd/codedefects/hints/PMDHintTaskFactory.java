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
package org.nbheaven.sqe.tools.pmd.codedefects.hints;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Florian Vogler
 */
@ServiceProvider(service = JavaSourceTaskFactory.class)
public final class PMDHintTaskFactory extends EditorAwareJavaSourceTaskFactory {

    public PMDHintTaskFactory() {
        super(JavaSource.Phase.UP_TO_DATE, JavaSource.Priority.MIN);
    }

    @Override
    protected CancellableTask<CompilationInfo> createTask(FileObject fileObject) {
        return new PMDHintTask();
    }

    static void rescheduleFile(FileObject file) {
        for (JavaSourceTaskFactory f : Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
            if (f instanceof PMDHintTaskFactory) {
                ((PMDHintTaskFactory) f).reschedule(file);
            }
        }
    }
}
