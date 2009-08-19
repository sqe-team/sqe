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
import com.sun.source.util.TreePathScanner;
import org.nbheaven.sqe.core.java.search.ClassElementDescriptor;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;

/**
 *
 * @author Sven Reimers
 */
public class SearchClassVisitor extends TreePathScanner<TreePathHandle, Void> {

    static enum State {
        DEAD_END, FINISHED, CONTINUE
    }
    
    private final CompilationInfo info;
    private final ClassElementDescriptor classElementDescriptor;
    private String[] classNameParts;
    private String actualName;
    private int classLevel = 0;
    
    private boolean inScope = false;
    
    private boolean anonymousMode = false;
    private int anonymousInnerCount = 0;
    private int anonymousInnerNumber = 0;

    
    private TreePathHandle handleFound;
    
    public SearchClassVisitor(CompilationInfo info, ClassElementDescriptor classElementDescriptor) {
        this.classElementDescriptor = classElementDescriptor;
        this.info = info;
        init();
    }

    private void init() {
        classNameParts = classElementDescriptor.getFQNClassName().split("[.$]");
        actualName = classNameParts[0];
    }

    private State advanceDepth() {
        if (classLevel == classNameParts.length - 1) {
            inScope = true;
            return State.FINISHED;
        } else {
            classLevel++;
            actualName = classNameParts[classLevel];
            if (actualName.matches("\\d+")) {
                anonymousMode = true;
                anonymousInnerNumber = Integer.parseInt(actualName);
                anonymousInnerCount = 0;
            } else {
                anonymousMode = false;
            }
            return State.CONTINUE;
        }
    }

    private State checkClass(ClassTree classTree) {
        if (anonymousMode) {
            anonymousInnerCount++;
            if (anonymousInnerCount == anonymousInnerNumber) {
                return advanceDepth();
            }            
        } else {
            if (classTree.getSimpleName().contentEquals(actualName)) {
                return advanceDepth();
            }
        }
        return State.DEAD_END;
    }

    boolean isInScope() {
        return inScope;
    }
    
    CompilationInfo getInfo() {
        return info;
    }
    
    void setHandleFound(TreePathHandle handleFound) {
        if (null == handleFound) {
            throw new IllegalArgumentException("Bad TreePathHandle - null is not an allowed value");
        }
        this.handleFound = handleFound;
    }

    TreePathHandle getHandleFound() {
        return this.handleFound;
    }

    boolean needsFurtherVisiting() {
        return false;
    }
    
    @Override
    public TreePathHandle visitClass(ClassTree classTree, Void arg1) {
        switch (checkClass(classTree)) {
            case CONTINUE:
                return super.visitClass(classTree, arg1);
            case FINISHED:
                setHandleFound(TreePathHandle.create(getCurrentPath(), info));
                if (needsFurtherVisiting()) {
                    TreePathHandle superHandle = super.visitClass(classTree, arg1);
                    return null != superHandle ? superHandle : getHandleFound();
                } else {                
                    return getHandleFound();
                }
            case DEAD_END:
                return getHandleFound();
        }
        throw new IllegalStateException("Bad traversal state");
    }

}
