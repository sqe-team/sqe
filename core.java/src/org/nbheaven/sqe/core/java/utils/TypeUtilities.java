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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;


/**
 * Various utilities to wrap Retouche
 * 
 * @author Sven Reimers
 */
public final class TypeUtilities {

    
    /**
     * Convert a TypeMirror to a string form as available in bytecode representation
     * e.g. [Ljava/lang/Integer;
     */
    public static String convertTypeMirrorToBinaryRepresentation(TypeMirror typeMirror) {
        StringBuilder builder = new StringBuilder();
        switch (typeMirror.getKind()) {
            case ARRAY:
                builder.append("[");
                return builder.append(convertTypeMirrorToBinaryRepresentation(((ArrayType) typeMirror).getComponentType())).toString();
            case BOOLEAN:
                builder.append("Z");
                break;
            case BYTE:
                builder.append("B");
                break;
            case CHAR:
                builder.append("C");
                break;
            case DOUBLE:
                builder.append("D");
                break;
            case FLOAT:
                builder.append("F");
                break;
            case INT:
                builder.append("I");
                break;
            case LONG:
                builder.append("J");
                break;
            case SHORT:
                builder.append("S");
                break;
            case VOID:
                builder.append("V");
                break;
            case TYPEVAR:
                builder.append(convertTypeMirrorToBinaryRepresentation(((TypeVariable)typeMirror).getUpperBound()));
                break;
            case DECLARED:
                builder.append("L");
                builder.append(((DeclaredType) typeMirror).asElement().toString().replaceAll("\\.", "/"));
                return builder.append(";").toString();
            default:
                builder.append("");
        }
        return builder.toString();
    }
    
    public static JavaSourceProvider getJavaTypeElement(String findIt, Project project) {
        String outerClass = findIt;
        // remove InnerclassDeclaration
        if (-1 != outerClass.indexOf('$')) {
            outerClass = outerClass.substring(0, outerClass.indexOf('$'));
        }
        
        // remove Package
        String javaClassName = outerClass.substring(outerClass.lastIndexOf(".")+1);
        

        for (SourceGroup g : ProjectUtilities.getJavaSourceGroups(project)) {
            FileObject root = g.getRootFolder();
            ClasspathInfo ci = ClasspathInfo.create(ClassPath.getClassPath(root, ClassPath.BOOT),
                    ClassPath.getClassPath(root, ClassPath.COMPILE),
                    ClassPath.getClassPath(root, ClassPath.SOURCE));
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
