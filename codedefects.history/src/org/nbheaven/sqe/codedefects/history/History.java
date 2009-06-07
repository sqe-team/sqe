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
package org.nbheaven.sqe.codedefects.history;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.prefs.Preferences;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Sven Reimers
 */
public final class History implements Iterable<History.Entry> {

    public static String PROP_HISTORY_CHANGED = "prop_history_changed";

    private final LinkedList<Entry> entries;
    private final Project project;

    private final PropertyChangeSupport propertyChangeSupport;

    private History(Project project, LinkedList<Entry> entries) {
        this.entries = entries;
        this.project = project;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public Project getProject() {
        return project;
    }

    public void appendEntry(Entry entry) {
        entries.add(entry);
        flush();
        fireHistoryChanged();
    }

    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener pcl) {
        this.propertyChangeSupport.addPropertyChangeListener(property, pcl);
    }

    public void removePropertyChangeListener(String property, PropertyChangeListener pcl) {
        this.propertyChangeSupport.removePropertyChangeListener(property, pcl);
    }

    private void fireHistoryChanged() {
        this.propertyChangeSupport.firePropertyChange(PROP_HISTORY_CHANGED, null, entries);
    }

    @Override
    public String toString() {
        return entries.toString();
    }

    public void clear() {
        entries.clear();
        flush();
        fireHistoryChanged();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private void flush() {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(os);
            stream.writeObject(entries);
            stream.close();
            Preferences historyPrefs = NbPreferences.forModule(History.class);
            historyPrefs.putByteArray(project.getProjectDirectory().getPath(), os.toByteArray());
        } catch (FileAlreadyLockedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static LinkedList<Entry> read(byte[] historyData) {
        InputStream is = null;
        LinkedList<Entry> entries = new LinkedList<Entry>();
        if (null != historyData && historyData.length > 0) {
            try {
                is = new ByteArrayInputStream(historyData);
                ObjectInputStream stream = new ObjectInputStream(is);
                entries.addAll((LinkedList<Entry>) stream.readObject());
                stream.close();
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return entries;
            }
        }
        return entries;
    }

    private static Map<Project, WeakReference<History>> historyCache = new HashMap<Project, WeakReference<History>>();

    public static synchronized History getHistory(Project project) {
        WeakReference<History> historyRef = historyCache.get(project);
        if (null != historyRef) {
            History history = historyRef.get();
            if (null != history) {
                return history;
            }
        }
        Preferences historyPrefs = NbPreferences.forModule(History.class);
        byte[] historyData = historyPrefs.getByteArray(project.getProjectDirectory().getPath(), new byte[] {});
        History history = new History(project, read(historyData));
        historyCache.put(project, new WeakReference<History>(history));
        return history;
    }

    public static final class Entry implements Serializable {

        static final long serialVersionUID = 42;

        private final QualityProviderStatisticSnapshot[] snapshots;

        private final Date date;

        public Entry(final Date date, QualityProviderStatisticSnapshot  ... snapshots) {
            this.date = date;
            this.snapshots = null == snapshots ? new QualityProviderStatisticSnapshot[] {} : snapshots;
        }

        public Date getDate() {
            return date;
        }

        public QualityProviderStatisticSnapshot[] getSnapshots() {
            return snapshots;
        }

        public QualityResultStatistic get(String ... providerIds) {
            long errors = 0;
            long warnings = 0;
            long infos = 0;

            for (QualityProviderStatisticSnapshot snapshot: getSnapshotsForProviders(providerIds)) {
                errors += snapshot.getErrors();
                warnings += snapshot.getWarnings();
                infos += snapshot.getInfos();
            }
            return new QualityResultStatisticEntry(errors, warnings, infos);
        }
        
        private QualityProviderStatisticSnapshot[] getSnapshotsForProviders (String ... ids) {
            Collection<QualityProviderStatisticSnapshot> selectedSnapshots = new ArrayList<QualityProviderStatisticSnapshot>();
            for (String id: ids) {
                for (QualityProviderStatisticSnapshot snapshot: snapshots) {
                    if(snapshot.providerId.equals(id)) {
                        selectedSnapshots.add(snapshot);
                    }
                }
            }
            return selectedSnapshots.toArray(new QualityProviderStatisticSnapshot[selectedSnapshots.size()]);
        }

        @Override
        public String toString() {
            return date + ": " + Arrays.toString(snapshots);
        }

        private static class QualityResultStatisticEntry implements QualityResultStatistic {
            private final long errors;
            private final long warnings;
            private final long infos;
            
            private final long sum;

            public QualityResultStatisticEntry(long errors, long warnings, long infos) {
                this.errors = errors;
                this.warnings = warnings;
                this.infos = infos;
                this.sum = infos + warnings + errors;
            }

            public long getCodeDefectCountSum() {
                return sum;
            }

            public long getCodeDefactCount(CodeDefectSeverity severity) {
                switch(severity) {
                    case ERROR : return errors;
                    case WARNING : return warnings;
                    case INFO : return infos;
                }
                return 0;
            }

        }
    }

    public static final class QualityProviderStatisticSnapshot implements Serializable {

        static final long serialVersionUID = 42;

        private final long errors;
        private final long warnings;
        private final long infos;

        private final String providerId;

        public QualityProviderStatisticSnapshot(final QualityProvider provider, final long errors, final long warnings, final long infos) {
            this.errors = errors;
            this.warnings = warnings;
            this.infos = infos;
            this.providerId = provider.getId();
        }

        public String getProviderId() {
            return providerId;
        }

        public long getErrors() {
            return errors;
        }

        public long getInfos() {
            return infos;
        }

        public long getWarnings() {
            return warnings;
        }

        @Override
        public String toString() {
            return providerId + ": E(" + errors + ") W(" + warnings +") I(" + infos +")";
        }
    }

}
