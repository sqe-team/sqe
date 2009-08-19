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
package org.nbheaven.sqe.tools.findbugs.codedefects.core.search.impl;

import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import org.nbheaven.sqe.core.java.search.ClassElementDescriptor;
import org.nbheaven.sqe.core.java.search.MethodElementDescriptor;
import org.nbheaven.sqe.core.java.utils.JavaSourceProvider;
import org.netbeans.api.project.Project;

/**
 *
 * @author Sven Reimers
 */
public final class MethodElementDescriptorImpl implements MethodElementDescriptor{
        
    private final ClassElementDescriptor classElementDescriptor;
    private final MethodAnnotation methodAnnotation;
    
    public MethodElementDescriptorImpl(ClassAnnotation classAnnotation, MethodAnnotation methodAnnotation, Project project) {
        this.classElementDescriptor = new ClassElementDescriptorImpl(classAnnotation, project);        
        this.methodAnnotation = methodAnnotation;
    }

    public ClassElementDescriptor getClassElementDescriptor() {
        return this.classElementDescriptor;
    }

    public JavaSourceProvider getSourceProvider() {
        return this.classElementDescriptor.getSourceProvider();
    }
    
    public String getName() {
        return methodAnnotation.getMethodName();
    }

    public String getSignature() {
        return methodAnnotation.getMethodSignature();
    }

    @Override
    public String toString() {
        return "MethodElementDescriptor: <" + this.classElementDescriptor + "> for method " + getName() + ":" + getSignature() ;
    }

}
