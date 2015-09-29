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
import org.nbheaven.sqe.core.java.search.ClassElementDescriptor;
import org.nbheaven.sqe.core.java.utils.JavaSourceProvider;
import org.nbheaven.sqe.core.java.utils.TypeUtilities;
import org.netbeans.api.project.Project;


/**
 *
 * @author Sven Reimers
 */
public final class ClassElementDescriptorImpl implements ClassElementDescriptor{ 
    
    private final ClassAnnotation classAnnotation;
    private final JavaSourceProvider javaSourceProvider;
    
    public ClassElementDescriptorImpl(ClassAnnotation classAnnotation, Project project) {
        this.classAnnotation = classAnnotation;
        javaSourceProvider = TypeUtilities.getJavaTypeElement(classAnnotation.getClassName(), project);                
    }

    @Override
    public String getFQNClassName() {
        if (0 == classAnnotation.getPackageName().length()) {
            return classAnnotation.getClassName();
        }
        else {
            return classAnnotation.getClassName().replaceFirst(classAnnotation.getPackageName() + ".", "");
        }
    }

    @Override
    public JavaSourceProvider getSourceProvider() {
        return javaSourceProvider;
    }

    @Override
    public String toString() {
        return "ClassElementDescriptor: " + getFQNClassName() + " for file " + getSourceProvider().getFileObject();
    }
    
}
