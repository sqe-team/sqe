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
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.nbheaven.sqe.tools.checkstyle.codedefects.core.settings.CheckstyleSettings;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
public abstract class AbstractCheckstyleSettings implements CheckstyleSettings {

    private Properties customProperties = new Properties();

    public final String getPropertiesAsString() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            customProperties.store(os, "");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            return os.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    protected final Properties getCustomProperties() {
        return customProperties;
    }

}
