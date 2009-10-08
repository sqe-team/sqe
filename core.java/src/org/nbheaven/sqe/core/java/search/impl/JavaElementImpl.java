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
package org.nbheaven.sqe.core.java.search.impl;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import java.awt.EventQueue;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.swing.text.StyledDocument;
import org.nbheaven.sqe.core.java.search.ElementDescriptor;
import org.nbheaven.sqe.core.java.search.JavaElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
public final class JavaElementImpl implements JavaElement {

    private final FileObject fileObject;
    private final ElementHandle<?> elementHandle;

    public JavaElementImpl(ElementDescriptor elementDescriptor, ElementHandle<?> elementHandle) {
        if (null == elementHandle) {
            throw new IllegalArgumentException("Null ElementHandle not supported for JavaElementImpl, ensure it is set for <"+ String.valueOf(elementDescriptor) +">!");
        }
        this.fileObject = elementDescriptor.getSourceProvider().getFileObject();
        this.elementHandle = elementHandle;
    }

    public void open() {
        if (null != fileObject && null != elementHandle) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    ElementOpen.open(fileObject, elementHandle);
                }
            });
        }
    }

    private int beginColumn = -1;
    private int endColumn = -1;

    public int getBeginColumn() {
        return beginColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public Line getLine() {
        try {
            DataObject dao = DataObject.find(fileObject);
            EditorCookie editorCookie = dao.getLookup().lookup(EditorCookie.class);
            LineCookie lineCookie = dao.getLookup().lookup(LineCookie.class);

            int[] offset = getOffset();
            int startOffset = offset[0];
            int endOffset = offset[1];

            if (editorCookie != null && lineCookie != null && startOffset != -1) {
                StyledDocument doc = editorCookie.openDocument();
                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, startOffset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    beginColumn = startOffset - lineOffset;

                    if (endOffset != -1) {
                        int lineEnd = NbDocument.findLineNumber(doc, endOffset);
                        int lineOffsetEnd = NbDocument.findLineOffset(doc, lineEnd);
                        endColumn = endOffset - lineOffsetEnd;
                    }
                    if (line != -1) {
                        return lineCookie.getLineSet().getCurrent(line);
                    }
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return null;
        }

        return null;

    }

    private int[] getOffset() throws IOException {
        final int[]  result = new int[] {-1, -1};

        JavaSource js = JavaSource.forFileObject(fileObject);
        if (js != null) {
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController info) {
                    try {
                        info.toPhase(JavaSource.Phase.RESOLVED);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                    Element el = elementHandle.resolve(info);
                    if (el == null) {
                        ErrorManager.getDefault().log(ErrorManager.ERROR, "Cannot resolve " + elementHandle + ". " + info.getClasspathInfo());
                        return;
                    }

                    FindDeclarationVisitor v = new FindDeclarationVisitor(el, info);

                    CompilationUnitTree cu = info.getCompilationUnit();

                    v.scan(cu, null);
                    Tree elTree = v.declTree;

                    if (elTree != null)
                        result[0] = (int)info.getTrees().getSourcePositions().getStartPosition(cu, elTree);
                        result[1] = (int)info.getTrees().getSourcePositions().getEndPosition(cu, elTree);
                }
            },true);
        }
        return result;
    }

    private static class FindDeclarationVisitor extends TreePathScanner<Void, Void> {

        private Element element;
        private Tree declTree;
        private CompilationInfo info;

        public FindDeclarationVisitor(Element element, CompilationInfo info) {
            this.element = element;
            this.info = info;
        }

	@Override
        public Void visitClass(ClassTree tree, Void d) {
            handleDeclaration();
            super.visitClass(tree, d);
            return null;
        }

	@Override
        public Void visitMethod(MethodTree tree, Void d) {
            handleDeclaration();
            super.visitMethod(tree, d);
            return null;
        }

	@Override
        public Void visitVariable(VariableTree tree, Void d) {
            handleDeclaration();
            super.visitVariable(tree, d);
            return null;
        }

        public void handleDeclaration() {
            Element found = info.getTrees().getElement(getCurrentPath());

            if ( element.equals( found ) ) {
                declTree = getCurrentPath().getLeaf();
            }
        }
    }
}
