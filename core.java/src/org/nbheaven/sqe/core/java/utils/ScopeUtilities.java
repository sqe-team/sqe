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
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Florian Vogler
 */
public class ScopeUtilities {

    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath(new FileObject[0]);

    public static Scope findScope(String fqnClassName, Project project) {

//        if (fqnClassName.startsWith("java.") || fqnClassName.startsWith("javax.")) {
//            return Scope.JDK;
//        }

        if (isProjectClass(fqnClassName, project)) {
            return Scope.PROJECT;
        }

        if (isJDKClass(fqnClassName, project)) {
            return Scope.JDK;

//        } else if (is3rdPartyClass(fqnClassName, project)) {
//            return Scope.EXTERNAL;
        }
        return Scope.EXTERNAL;
    }

    public static boolean isJDKClass(String fqnClassName, Project project) {
        String outerClass = fqnClassName;
        // remove InnerclassDeclaration
        if (-1 != outerClass.indexOf('$')) {
            outerClass = outerClass.substring(0, outerClass.indexOf('$'));
        }

        // remove Package
        String javaClassName = outerClass.substring(outerClass.lastIndexOf(".") + 1);

        for (SourceGroup g : ProjectUtilities.getJavaSourceGroups(project)) {
            ClasspathInfo ci = ClasspathInfo.create(ClassPath.getClassPath(g.getRootFolder(), ClassPath.BOOT),
                    EMPTY_CLASSPATH, EMPTY_CLASSPATH);
            Set<ElementHandle<TypeElement>> declaredTypes = ci.getClassIndex().getDeclaredTypes(javaClassName,
                    ClassIndex.NameKind.SIMPLE_NAME, EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES));
            for (ElementHandle<TypeElement> typeElementHandle : declaredTypes) {
                if (typeElementHandle.getQualifiedName().equals(outerClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean is3rdPartyClass(String fqnClassName, Project project) {
        return !(isProjectClass(fqnClassName, project) || isJDKClass(fqnClassName, project));
    }

    public static boolean isProjectClass(String fqnClassName, Project project) {
        String outerClass = fqnClassName;
        // remove InnerclassDeclaration
        if (-1 != outerClass.indexOf('$')) {
            outerClass = outerClass.substring(0, outerClass.indexOf('$'));
        }

        // remove Package
        String javaClassName = outerClass.substring(outerClass.lastIndexOf(".") + 1);

        for (SourceGroup g : ProjectUtilities.getJavaSourceGroups(project)) {
            ClasspathInfo ci = ClasspathInfo.create(EMPTY_CLASSPATH, EMPTY_CLASSPATH,
                    ClassPath.getClassPath(g.getRootFolder(), ClassPath.SOURCE));
            Set<ElementHandle<TypeElement>> declaredTypes = ci.getClassIndex().getDeclaredTypes(javaClassName,
                    ClassIndex.NameKind.SIMPLE_NAME, EnumSet.of(ClassIndex.SearchScope.SOURCE));
            for (ElementHandle<TypeElement> typeElementHandle : declaredTypes) {
                if (typeElementHandle.getQualifiedName().equals(outerClass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
