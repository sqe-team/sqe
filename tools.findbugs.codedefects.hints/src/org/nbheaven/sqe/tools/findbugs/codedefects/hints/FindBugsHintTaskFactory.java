/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbheaven.sqe.tools.findbugs.codedefects.hints;

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
 * @author fvo
 */
@ServiceProvider(service = JavaSourceTaskFactory.class)
public final class FindBugsHintTaskFactory extends EditorAwareJavaSourceTaskFactory {

    public FindBugsHintTaskFactory() {
        super(JavaSource.Phase.UP_TO_DATE, JavaSource.Priority.MIN);
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

}
