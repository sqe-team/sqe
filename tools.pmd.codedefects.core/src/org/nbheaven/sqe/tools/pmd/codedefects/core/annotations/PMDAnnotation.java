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
package org.nbheaven.sqe.tools.pmd.codedefects.core.annotations;

import org.netbeans.api.project.Project;

import org.openide.text.Annotation;
import org.openide.text.Line;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An annotation implementation marking the line where a FindBug was detected.
 * This class tracks all constructed instances of this annotation type, and can remove
 * any of them. It listens to property-change events from the lines at which the
 * annotations are attached, and removes the annotations when the lines change or are
 * removed.
 */
public class PMDAnnotation extends Annotation implements PropertyChangeListener {
    /** The annotations currently existing. */
    private static Map<Project, List<Annotation>> annotationMap = new HashMap<Project, List<Annotation>>();

    /** The error message shown on mouseover on the pmd icon */
    private String errormessage = null;

    private PMDAnnotation() {
    }

    public static final PMDAnnotation getNewInstance(Project project) {
        PMDAnnotation pmd = new PMDAnnotation();
        List<Annotation> annotations = annotationMap.get(project);

        if (null == annotations) {
            annotations = new ArrayList<Annotation>();
            annotationMap.put(project, annotations);
        }

        annotations.add(pmd);

        return pmd;
    }

    public static final void clearAll(Project project) {
        List<Annotation> annotations = annotationMap.get(project);

        if (null != annotations) {
            for (Annotation annotation : annotations) {
                annotation.detach();
            }

            annotations.clear();
        }
    }

    /**
     * The annotation type.
     *
     * @return the string "findbugs-annotation"
     */
    public String getAnnotationType() {
        return "pmd-annotation";
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
    public String getShortDescription() {
        return errormessage;
    }

    /**
     * Invoked when the user change the content on the line where the annotation is
     * attached
     *
     * @param propertyChangeEvent the event fired
     */
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getPropertyName().equals("annotationCount")) {
            return;
        }
        Line line = (Line) propertyChangeEvent.getSource();
        line.removePropertyChangeListener(this);
        detach();
    }
}
