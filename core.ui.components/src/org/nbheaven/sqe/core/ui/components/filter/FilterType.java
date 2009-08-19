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
package org.nbheaven.sqe.core.ui.components.filter;

import javax.swing.ImageIcon;

/**
 *
 * @author Sven Reimers
 */
public enum FilterType {

    NONE,
    STARTS_WITH {

        private ImageIcon icon = new ImageIcon(this.getClass().getResource("/org/nbheaven/sqe/core/ui/components/resources/filterStartsWith.png"));

        @Override
        public ImageIcon getIcon() {
            return icon;
        }

        @Override
        public String getDisplayName() {
            return "Starts with";
        }

        @Override
        protected boolean acceptImpl(String item, String rule) {
            return null == item ? false : item.trim().startsWith(rule);
        }
    },
    CONTAINS {

        private ImageIcon icon = new ImageIcon(this.getClass().getResource("/org/nbheaven/sqe/core/ui/components/resources/filterContains.png"));

        @Override
        public ImageIcon getIcon() {
            return icon;
        }

        @Override
        public String getDisplayName() {
            return "Contains";
        }

        @Override
        protected boolean acceptImpl(String item, String rule) {
            return null == item ? false : item.contains(rule);
        }
    },
    ENDS_WITH {

        private ImageIcon icon = new ImageIcon(this.getClass().getResource("/org/nbheaven/sqe/core/ui/components/resources/filterEndsWith.png"));

        @Override
        public ImageIcon getIcon() {
            return icon;
        }

        @Override
        public String getDisplayName() {
            return "Ends With";
        }

        @Override
        protected boolean acceptImpl(String item, String rule) {
            return null == item ? false : item.trim().endsWith(rule);
        }
    },
    REGEXP {

        private ImageIcon icon = new ImageIcon(this.getClass().getResource("/org/nbheaven/sqe/core/ui/components/resources/filterRegExp.png"));

        @Override
        public ImageIcon getIcon() {
            return icon;
        }

        @Override
        public String getDisplayName() {
            return "Regular Expression";
        }

        @Override
        protected boolean acceptImpl(String item, String rule) {
            return null == item ? false : item.trim().matches(rule);
        }
    },
    UNDEFINED;

    public ImageIcon getIcon() {
        return null;
    }

    public String getDisplayName() {
        return "None";
    }

    public final boolean accept(String item, String... rules) {

        for (String rule : rules) {
            if (acceptImpl(item, rule)) {
                return true;
            }
        }
        return false;
    }

    protected boolean acceptImpl(String item, String rule) {
        return false;
    }
}
