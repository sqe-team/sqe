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

import java.awt.Toolkit;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;

import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;

import javax.lang.model.element.TypeElement;


public final class JavaSourceProvider {
    private ClasspathInfo classpathInfo;
    private ElementHandle<TypeElement> handle;

    JavaSourceProvider(ClasspathInfo classpathInfo, ElementHandle<TypeElement> handle) {
        super();
        this.classpathInfo = classpathInfo;
        this.handle = handle;
    }

    public void open() {
        if (!ElementOpen.open(getFileObject(), handle)) {
            StatusDisplayer.getDefault()
                           .setStatusText("No Java Source file found " + handle.getQualifiedName());
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public FileObject getFileObject() {
        return SourceUtils.getFile(handle, classpathInfo);
    }

    public ElementHandle<TypeElement> getTypeHandle() {
        return handle;
    }

    public ClasspathInfo getClasspathInfo() {
        return this.classpathInfo;
    }
}
