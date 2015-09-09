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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.annotations;

import org.netbeans.api.project.Project;

import org.openide.text.Annotation;
import org.openide.text.Line;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An annotation implementation marking the line where a Checkstyle Audit was
 * detected. This class tracks all constructed instances of this annotation
 * type, and can remove any of them. It listens to property-change events from
 * the lines at which the annotations are attached, and removes the annotations
 * when the lines change or are removed.
 */
public class CheckstyleAnnotation extends Annotation implements PropertyChangeListener {

    /**
     * The annotations currently existing.
     */
    private static final Map<Project, List<CheckstyleAnnotation>> annotationMap = new ConcurrentHashMap<>();
    private static final ReentrantLock secureGetCreateLock = new ReentrantLock();

    /**
     * The error message shown on mouseover on the checkstyle icon
     */
    private String errormessage = null;

    private CheckstyleAnnotation() {
    }

    public static final CheckstyleAnnotation getNewInstance(Project project) {
        List<CheckstyleAnnotation> annotations;

        secureGetCreateLock.lock();
        try {
            annotations = annotationMap.get(project);

            if (null == annotations) {
                annotations = new ArrayList<>();
                annotationMap.put(project, annotations);
            }
        } finally {
            secureGetCreateLock.unlock();
        }
        CheckstyleAnnotation pmd = new CheckstyleAnnotation();

        synchronized (annotations) {
            annotations.add(pmd);
        }

        return pmd;
    }

    public static final void clearAll(Project project) {
        List<CheckstyleAnnotation> annotations = annotationMap.get(project);

        if (null != annotations) {
            synchronized (annotations) {
                annotations.stream().forEach(annotation -> annotation.detach());
                annotations.clear();
            }
        }
    }

    /**
     * The annotation type.
     *
     * @return the string "checkstyle-annotation"
     */
    @Override
    public String getAnnotationType() {
        return "checkstyle-annotation";
    }

    /**
     * Sets the current errormessage
     *
     * @param message the errormessage
     */
    public void setErrorMessage(String message) {
        errormessage = message;
    }

    /**
     * A short description of this annotation
     *
     * @return the short description
     */
    @Override
    public String getShortDescription() {
        return errormessage;
    }

    /**
     * Invoked when the user change the content on the line where the annotation
     * is attached
     *
     * @param propertyChangeEvent the event fired
     */
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getPropertyName().equals("annotationCount")) {
            return;
        }
        Line line = (Line) propertyChangeEvent.getSource();
        line.removePropertyChangeListener(this);
        detach();
    }
}
