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
package org.nbheaven.sqe.core.java.search;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import org.nbheaven.sqe.core.java.search.impl.JavaElementImpl;
import org.nbheaven.sqe.core.java.search.impl.SearchClassVisitor;
import org.nbheaven.sqe.core.java.search.impl.SearchMethodVisitor;
import org.nbheaven.sqe.core.java.search.impl.SearchVariableVisitor;
import org.nbheaven.sqe.core.java.utils.JavaSourceProvider;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
public final class SearchUtilities {

    private SearchUtilities() {
    }

    public static JavaElement findClassElement(final ClassElementDescriptor descriptor) {
        return new Searcher() {
            @Override
            protected TreePathScanner<TreePathHandle, Void> makeVisitor(CompilationController controller) {
                return new SearchClassVisitor(controller, descriptor);
            }
        }.findElement(descriptor);
    }

    public static JavaElement findMethodElement(final MethodElementDescriptor descriptor) {
        return new Searcher() {
            @Override
            protected TreePathScanner<TreePathHandle, Void> makeVisitor(CompilationController controller) {
                return new SearchMethodVisitor(controller, descriptor);
            }
        }.findElement(descriptor);
    }

    public static JavaElement findVariableElement(final VariableElementDescriptor descriptor) {
        return new Searcher() {
            @Override
            protected TreePathScanner<TreePathHandle, Void> makeVisitor(CompilationController controller) {
                return new SearchVariableVisitor(controller, descriptor);
            }
        }.findElement(descriptor);
    }

    private static abstract class Searcher implements CancellableTask<CompilationController> {
        private ElementHandle<?> elementHandle;
        protected abstract TreePathScanner<TreePathHandle,Void> makeVisitor(CompilationController controller);
        public JavaElement findElement(ElementDescriptor descriptor) {
            if (descriptor == null) {
                return null;
            }
            JavaSourceProvider sourceProvider = descriptor.getSourceProvider();
            if (sourceProvider == null) {
                return null;
            }
            FileObject fileObject = sourceProvider.getFileObject();
            if (fileObject == null) {
                return null;
            }
            try {
                JavaSource javaSource = JavaSource.forFileObject(fileObject);
                if (javaSource == null) {
                    return null;
                }
                javaSource.runUserActionTask(this, false);
                if (elementHandle != null) {
                    return new JavaElementImpl(descriptor, elementHandle);
                }
            } catch (IOException e) {
                Logger.getLogger(SearchUtilities.class.getName()).log(Level.INFO, "Bad search for class element", e);
            }
            return null;
        }
        @Override
        public final void run(CompilationController controller) throws Exception {
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            TreePathHandle handle = makeVisitor(controller).scan(controller.getCompilationUnit(), null);
            if (handle != null) {
                Element element = handle.resolveElement(controller);
                if (element != null) {
                    elementHandle = ElementHandle.create(element);
                }
            }
        }
        @Override
        public final void cancel() {}
    }

    public static Collection<String> getFQNClassNames(final FileObject fo) {
        if (fo == null || !fo.isValid() || fo.isVirtual()) {
            return Collections.<String>emptyList();
        }
        final JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return Collections.<String>emptyList();
        }
        try {
            final Collection<String> result = new ArrayList<String>();
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                @Override
                public void run(final CompilationController control) throws Exception {
                    if (control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) >= 0) {
                        ClassVisitor visitor = new ClassVisitor(control);
                        visitor.scan(control.getCompilationUnit(), null);
                        result.addAll(visitor.getClassNames());
                    }
                }

                @Override
                public void cancel() {
                }
            }, true);
            return result;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return Collections.<String>emptyList();
        }
    }

    private static class NameDesc {

        String name = "";
        int inner = 0;

        public NameDesc(String name) {
            this.name = name;
        }
    }

    private static class ClassVisitor extends TreePathScanner<Void, NameDesc> {

        private String packageName;
        private Collection<String> classNames;

        public ClassVisitor(CompilationInfo info) {
            ExpressionTree packageNameTree = info.getCompilationUnit().getPackageName();
            if (null != packageNameTree) {
                this.packageName = packageNameTree.toString();
            }
            classNames = new ArrayList<String>();
        }

        @Override
        public Void visitClass(ClassTree t, NameDesc name) {
            if (null == name) {
                name = new NameDesc(t.getSimpleName().toString());
            } else {
                if (!t.getSimpleName().contentEquals("")) {
                    name = new NameDesc(name.name + "$" + t.getSimpleName().toString());
                }
            }
            classNames.add(null != packageName ? packageName + "." + name.name : name.name);
            return super.visitClass(t, name);
        }

        @Override
        public Void visitNewClass(NewClassTree newClassTree, NameDesc name) {
            name.inner++;
            name = new NameDesc(name.name + "$" + String.valueOf(name.inner));
            return super.visitNewClass(newClassTree, name);
        }

        private Collection<String> getClassNames() {
            return classNames;
        }
    }

}
