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
package org.nbheaven.sqe.tools.findbugs.codedefects.hints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.FindBugsQualityProvider;
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
public final class FindBugsHintTaskFactory extends EditorAwareJavaSourceTaskFactory implements PropertyChangeListener {

    private final PropertyChangeListener listener;

    public FindBugsHintTaskFactory() {
        super(JavaSource.Phase.UP_TO_DATE, JavaSource.Priority.MIN);
        listener = FindBugsQualityProvider.getDefault().getSessionEventProxy().addWeakPropertyChangeListener(this);
    }

    @Override
    protected CancellableTask<CompilationInfo> createTask(FileObject fileObject) {
        return new FindBugsHintTask();
    }

    static void rescheduleFile(FileObject file) {
        for (JavaSourceTaskFactory f : Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
            if (f instanceof FindBugsHintTaskFactory) {
                ((FindBugsHintTaskFactory) f).reschedule(file);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (QualityProvider.SessionEventProxy.BACKGROUND_SCANNING_EFFECTIVE_ENABLED_PROPERTY.equals(evt.getPropertyName())) {
            QualitySession session = (QualitySession) evt.getSource();
            for (FileObject fileObject : getFileObjects()) {
                if (session.getProject().equals(SQEProjectSupport.findProjectByFileObject(fileObject))) {
                    reschedule(fileObject);
                }
            }
        }
    }

}
