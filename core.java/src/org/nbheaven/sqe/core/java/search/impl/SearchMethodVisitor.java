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

import com.sun.source.tree.MethodTree;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.nbheaven.sqe.core.java.search.MethodElementDescriptor;
import org.nbheaven.sqe.core.java.utils.TypeUtilities;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;


/**
 *
 * @author Sven Reimers
 */
public class SearchMethodVisitor extends SearchClassVisitor {
    
    private MethodElementDescriptor descriptor;

    public SearchMethodVisitor(CompilationInfo info, MethodElementDescriptor descriptor) {
        super(info, descriptor.getClassElementDescriptor());
        this.descriptor = descriptor;
    }

    private boolean checkMethod(MethodTree methodTree) {
        if (methodTree.getName().contentEquals(descriptor.getName())) { 
            Element el = TreePathHandle.create(getCurrentPath(), getInfo()).resolveElement(getInfo());
            if (el.getKind().equals(ElementKind.METHOD) || el.getKind().equals(ElementKind.CONSTRUCTOR)) {
                ExecutableElement method = ((ExecutableElement)TreePathHandle.create(getCurrentPath(), getInfo()).resolveElement(getInfo()));
                StringBuilder builder = new StringBuilder("(");
                for (VariableElement variableElement: method.getParameters()) {
                    builder.append(TypeUtilities.convertTypeMirrorToBinaryRepresentation(variableElement.asType()));
                }
                builder.append(")");
                builder.append(TypeUtilities.convertTypeMirrorToBinaryRepresentation(method.getReturnType()));
                if (descriptor.getSignature().equals(builder.toString())){
                    return true;
                }              
            }
        }
        return false;
    }
    
    @Override
    boolean needsFurtherVisiting() {
        return true;
    }        
    @Override
    public TreePathHandle visitMethod(MethodTree methodTree, Void voidArg) {
        // check for correct depth in class
        if (isInScope()) {
            // ok - one of the following should match 
            if (checkMethod(methodTree)) {
                setHandleFound(TreePathHandle.create(getCurrentPath(), getInfo()));
            }     
            return getHandleFound();            
        } else {
            // do not kill recursion here, else we may miss anonymous inner classes
            TreePathHandle handle = super.visitMethod(methodTree, voidArg);
            return handle;
        }
    }
    
}
