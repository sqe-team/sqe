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
package org.nbheaven.sqe.core.maven.spi;

import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.nbheaven.sqe.core.maven.api.MavenPluginConfiguration;
import org.openide.util.Exceptions;

/**
 *
 * @author Sven Reimers
 */
@Deprecated
public class MavenPluginConfigurationImpl implements MavenPluginConfiguration {

    private final Xpp3Dom mavenConfiguration;
    private final ExpressionEvaluator eval;

    public MavenPluginConfigurationImpl(Xpp3Dom mavenConfiguration, ExpressionEvaluator eval) {
        this.mavenConfiguration = mavenConfiguration;
        this.eval = eval;
    }

    public String getValue(String path) {

        Xpp3Dom source = mavenConfiguration.getChild(path);
        if (null == source) {
            return "";
        }
        try {
            Object evaluated = eval.evaluate(source.getValue().trim());
            return evaluated != null ? ("" + evaluated) : source.getValue().trim(); //NOI18N
        } catch (ExpressionEvaluationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return source.getValue().trim();
    }

    public String[] getStringListValue(String listParent, String listChild) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isDefinedInProject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
