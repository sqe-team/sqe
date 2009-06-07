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
package org.nbheaven.sqe.tools.depfinder.dependencies.core.model;

public final class DependencyNode {

    private final ModelRef modelRef;
    private final String source;
    private final String target;

    public DependencyNode(ModelRef modelRef, String source, String target) {
        this.modelRef = modelRef;
        this.source = source;
        this.target = target;
    }

    protected DepFinderModel getModel() {
        return modelRef.getModel();
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "Dependency [" + source + " ->  " + target + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DependencyNode other = (DependencyNode) obj;
        if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
            return false;
        }
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.source != null ? this.source.hashCode() : 0);
        hash = 97 * hash + (this.target != null ? this.target.hashCode() : 0);
        return hash;
    }
}
