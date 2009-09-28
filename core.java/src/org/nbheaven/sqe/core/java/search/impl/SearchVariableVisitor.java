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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import javax.lang.model.element.Element;
import org.nbheaven.sqe.core.java.search.VariableElementDescriptor;
import org.nbheaven.sqe.core.java.utils.TypeUtilities;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;


/**
 *
 * @author sven
 */
public class SearchVariableVisitor extends SearchClassVisitor {
    
    private VariableElementDescriptor descriptor;

    public SearchVariableVisitor(CompilationInfo info, VariableElementDescriptor descriptor) {
        super(info, descriptor.getClassElementDescriptor());
        this.descriptor = descriptor;
    }

    private boolean checkVariable(VariableTree variableTree) {
        if (!getCurrentPath().getParentPath().getLeaf().getKind().equals(Kind.CLASS)) {
            return false;
        }
        if (variableTree.getName().contentEquals(descriptor.getName())) {            
            Element element = TreePathHandle.create(getCurrentPath(), getInfo()).resolveElement(getInfo());
            if (TypeUtilities.convertTypeMirrorToBinayRepresentation(element.asType()).equals(descriptor.getSignature())) {
                return true;
            }        
        }
        return false;
    }
    
    @Override
    boolean needsFurtherVisiting() {
        return true;
    }        
    
    @Override
    public TreePathHandle visitVariable(VariableTree variableTree, Void voidArg) {
        // check for correct depth in class
        if (isInScope()) {
            // ok - one of the following should match 
            if (checkVariable(variableTree)) {
                setHandleFound(TreePathHandle.create(getCurrentPath(), getInfo()));
            }     
            return getHandleFound();            
        } else {
            // do not kill recursion here, else we may miss anonymous inner classes
            return super.visitVariable(variableTree, voidArg);
        }
    }
    
}
