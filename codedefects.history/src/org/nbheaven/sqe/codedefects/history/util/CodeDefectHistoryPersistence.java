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
package org.nbheaven.sqe.codedefects.history.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectProperties;
import org.nbheaven.sqe.codedefects.history.History;
import org.nbheaven.sqe.codedefects.history.History.QualityProviderStatisticSnapshot;
import org.netbeans.api.project.Project;

/**
 *
 * @author Sven Reimers
 */
public final class CodeDefectHistoryPersistence {

    public static void addSnapshot(Project project) {
        History.Entry entry = new History.Entry(new Date(), createSnapshot(project));
        History.getHistory(project).appendEntry(entry);
    }

    private static QualityProviderStatisticSnapshot[] createSnapshot(Project project) {
        Collection<QualityProviderStatisticSnapshot> snapshots = new ArrayList<QualityProviderStatisticSnapshot>();
        for (QualitySession session: project.getLookup().lookupAll(QualitySession.class)) {
            if (SQECodedefectProperties.isQualityProviderActive(project, session.getProvider())) {
                QualityResult result = session.getResult();
                if (null != result) {
                    QualityResultStatistic statistic = result.getLookup().lookup(QualityResultStatistic.class);
                    long errors = statistic.getCodeDefactCount(CodeDefectSeverity.ERROR);
                    long warns = statistic.getCodeDefactCount(CodeDefectSeverity.WARNING);
                    long infos = statistic.getCodeDefactCount(CodeDefectSeverity.INFO);
                    snapshots.add(new QualityProviderStatisticSnapshot(session.getProvider(), errors, warns, infos));
                }
            }
        }
        return snapshots.toArray(new QualityProviderStatisticSnapshot[snapshots.size()]);
    }
    
}
