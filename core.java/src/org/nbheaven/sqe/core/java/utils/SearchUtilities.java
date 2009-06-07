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
package org.nbheaven.sqe.core.java.utils;

import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;


/**
 *
 * @author Sven Reimers
 */
public class SearchUtilities {
    
    public static JavaSourceProvider findJavaSourceByFQN(String findIt, Project project) {
        ClassPathProvider cpp = project.getLookup().lookup(org.netbeans.spi.java.classpath.ClassPathProvider.class);

        String outerClass = findIt;
        // remove InnerclassDeclaration
        if (-1 != outerClass.indexOf('$')) {
            outerClass = outerClass.substring(0, outerClass.indexOf('$'));
        }
        
        // remove Package
        String javaClassName = outerClass.substring(outerClass.lastIndexOf(".")+1);
        

        Sources s = ProjectUtils.getSources(project);

        SourceGroup[] sourceGroups = s.getSourceGroups("java");
        // sourcePath
        for (int i = 0; i < sourceGroups.length; i++) {
            ClasspathInfo ci = ClasspathInfo.create(cpp.findClassPath(sourceGroups[i].getRootFolder(), ClassPath.BOOT), 
                    cpp.findClassPath(sourceGroups[i].getRootFolder(), ClassPath.COMPILE), 
                    cpp.findClassPath(sourceGroups[i].getRootFolder(), ClassPath.SOURCE));
            Set<ElementHandle<TypeElement>> declaredTypes = ci.getClassIndex().getDeclaredTypes(javaClassName,
                    ClassIndex.NameKind.SIMPLE_NAME, EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
            for(ElementHandle<TypeElement> typeElementHandle: declaredTypes) {
                if (typeElementHandle.getQualifiedName().equals(outerClass)) {
                    return new JavaSourceProvider(ci, typeElementHandle);
                }
            }
        }                        
        return null;
    }

}
