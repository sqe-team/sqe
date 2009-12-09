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
package org.nbheaven.sqe.tools.findbugs.codedefects.core;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.HashMap;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.utils.FiBuUtil;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.nbheaven.sqe.codedefects.core.api.CodeDefectSeverity;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.annotations.BugAnnotationProcessor;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Sven Reimers
 */
@SuppressWarnings("IS2_INCONSISTENT_SYNC")
public class FindBugsResult implements QualityResult, QualityResultStatistic {

    public enum Mode {

        CLASS("HINT_VIEW_BY_CLASS", "org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/class.gif") {

            public Map<?, Collection<BugInstance>> getInstanceList(final FindBugsResult result, boolean coreBugsOnly) {
                return result.getInstanceByClass(coreBugsOnly);
            }
        },
        PACKAGE("HINT_VIEW_BY_PACKAGE", "org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/package.gif") {

            public Map<?, Collection<BugInstance>> getInstanceList(final FindBugsResult result, boolean coreBugsOnly) {
                return result.getInstanceByPackage(coreBugsOnly);
            }
        },
        CATEGORY("HINT_VIEW_BY_CATEGORY", "edu/umd/cs/findbugs/gui/bug.png") {

            public Map<?, Collection<BugInstance>> getInstanceList(final FindBugsResult result, boolean coreBugsOnly) {
                return result.getInstanceByCategory(coreBugsOnly);
            }
        },
        TYPE("HINT_VIEW_BY_CATEGORY", "edu/umd/cs/findbugs/gui/bug2.png") {

            public Map<?, Collection<BugInstance>> getInstanceList(final FindBugsResult result, boolean coreBugsOnly) {
                return result.getInstanceByType(coreBugsOnly);
            }
        };
        private final String hint;
        private final Icon icon;

        Mode(String hint, String iconPath) {
            this.hint = hint;
            icon = new ImageIcon(ImageUtilities.loadImage(iconPath));
        }

        public String getHint() {
            return hint;
        }

        public Icon getIcon() {
            return icon;
        }

        public abstract Map<?, Collection<BugInstance>> getInstanceList(final FindBugsResult result, boolean coreBugsOnly);
    }
    private Map<ClassKey, Collection<BugInstance>> instanceByClass = new TreeMap<ClassKey, Collection<BugInstance>>();
    private Map<PackageKey, Collection<BugInstance>> instanceByPackage = new TreeMap<PackageKey, Collection<BugInstance>>();
    private Map<CategoryKey, Collection<BugInstance>> instanceByCategory = new TreeMap<CategoryKey, Collection<BugInstance>>();
    private Map<BugPattern, Collection<BugInstance>> instanceByType = new TreeMap<BugPattern, Collection<BugInstance>>();
    private Map<ClassKey, Collection<BugInstance>> filteredInstanceByClass;
    private Map<PackageKey, Collection<BugInstance>> filteredInstanceByPackage;
    private Map<CategoryKey, Collection<BugInstance>> filteredInstanceByCategory;
    private Map<BugPattern, Collection<BugInstance>> filteredInstanceByType;
    private long bugCount = 0;
    private long coreBugCount = 0;
    private long infoBugCount = 0;
    private long warningBugCount = 0;
    private long errorBugCount = 0;
    private Lookup lookup;
    private final FindBugsSession session;

    /** Creates a new instance of FindBugsResult */
    FindBugsResult(Project project) {
        lookup = Lookups.singleton(this);
        this.session = project.getLookup().lookup(FindBugsSession.class);
    }

    public Lookup getLookup() {
        return lookup;
    }

    private <T> void removeAllBugInstancesForBugPattern(BugPattern bugPattern, Map<T, Collection<BugInstance>> mapToClear) {
        for (Map.Entry<T, Collection<BugInstance>> entry : new HashMap<T, Collection<BugInstance>>(mapToClear).entrySet()) {
            for (BugInstance bugInstance : new ArrayList<BugInstance>(entry.getValue())) {
                if (bugInstance.getBugPattern().equals(bugPattern)) {
                    entry.getValue().remove(bugInstance);
                }
            }
            if (entry.getValue().isEmpty()) {
                mapToClear.remove(entry.getKey());
            }
        }
    }

    public void removeAllBugInstancesForBugPattern(BugPattern bugPattern) {
        if (null != instanceByType) {
            removeAllBugInstancesForBugPattern(bugPattern, instanceByType);
        }
        if (null != instanceByCategory) {
            removeAllBugInstancesForBugPattern(bugPattern, instanceByCategory);
        }
        if (null != instanceByClass) {
            removeAllBugInstancesForBugPattern(bugPattern, instanceByClass);
        }
        if (null != instanceByPackage) {
            removeAllBugInstancesForBugPattern(bugPattern, instanceByPackage);
        }



        session.resultChanged(null, this);
    }

    private Collection<BugInstance> storageOnly = new ArrayList<BugInstance>();
    private AtomicBoolean initialized = new AtomicBoolean(Boolean.FALSE);

    protected void add(final BugInstance bugInstance) {
        storageOnly.add(bugInstance);
    }

    private synchronized void reallyUpdateMaps() {
        if (initialized.compareAndSet(false, true)) {
            for (BugInstance bugInstance: storageOnly) {
                bugCount++;
                if (FiBuUtil.isBugPatternIssuedFromCore(bugInstance.getBugPattern())) {
                    coreBugCount++;
                }
                // register by classname
                ClassKey classKey = new ClassKey(bugInstance.getPrimaryClass(), session.getProject());
                Collection<BugInstance> listByClass = instanceByClass.get(classKey);
                if (null == listByClass) {
                    listByClass = new ArrayList<BugInstance>();
                    instanceByClass.put(classKey, listByClass);
                }
                listByClass.add(bugInstance);

                // register by packagename
                PackageKey packageKey = new PackageKey(bugInstance.getPrimaryClass());
                Collection<BugInstance> listByPackage = instanceByPackage.get(packageKey);
                if (null == listByPackage) {
                    listByPackage = new ArrayList<BugInstance>();
                    instanceByPackage.put(packageKey, listByPackage);
                }
                listByPackage.add(bugInstance);

                // register by category
                CategoryKey categoryKey = new CategoryKey(bugInstance.getBugPattern());
                Collection<BugInstance> listByCategory = instanceByCategory.get(categoryKey);
                if (null == listByCategory) {
                    listByCategory = new ArrayList<BugInstance>();
                    instanceByCategory.put(categoryKey, listByCategory);
                }
                listByCategory.add(bugInstance);

                // register by category
                Collection<BugInstance> listByType = instanceByType.get(bugInstance.getBugPattern());
                if (null == listByType) {
                    listByType = new ArrayList<BugInstance>();
                    instanceByType.put(bugInstance.getBugPattern(), listByType);
                }
                listByType.add(bugInstance);

                if (Priorities.HIGH_PRIORITY == bugInstance.getPriority()) {
                    errorBugCount++;
                } else if (Priorities.NORMAL_PRIORITY == bugInstance.getPriority()) {
                    warningBugCount++;
                } else {
                    infoBugCount++;
                }
            }
        }
    }

    public Map<ClassKey, Collection<BugInstance>> getInstanceByClass(boolean coreBugsOnly) {
        reallyUpdateMaps();
        if (coreBugsOnly) {
            if (null == filteredInstanceByClass) {
                filteredInstanceByClass = createFilteredMap(instanceByClass);
            }
            return filteredInstanceByClass;
        } else {
            return instanceByClass;
        }
    }

    public Map<PackageKey, Collection<BugInstance>> getInstanceByPackage(boolean coreBugsOnly) {
        reallyUpdateMaps();
        if (coreBugsOnly) {
            if (null == filteredInstanceByPackage) {
                filteredInstanceByPackage = createFilteredMap(instanceByPackage);
            }
            return filteredInstanceByPackage;
        } else {
            return instanceByPackage;
        }
    }

    public Map<CategoryKey, Collection<BugInstance>> getInstanceByCategory(boolean coreBugsOnly) {
        reallyUpdateMaps();
        if (coreBugsOnly) {
            if (null == filteredInstanceByCategory) {
                filteredInstanceByCategory = createFilteredMap(instanceByCategory);
            }
            return filteredInstanceByCategory;
        } else {
            return instanceByCategory;
        }
    }

    public Map<BugPattern, Collection<BugInstance>> getInstanceByType(boolean coreBugsOnly) {
        reallyUpdateMaps();
        if (coreBugsOnly) {
            if (null == filteredInstanceByType) {
                filteredInstanceByType = createFilteredMap(instanceByType);
            }
            return filteredInstanceByType;
        } else {
            return instanceByType;
        }
    }

    private <T> Map<T, Collection<BugInstance>> createFilteredMap(Map<T, Collection<BugInstance>> originalMap) {
        Map<T, Collection<BugInstance>> filteredMap = new TreeMap<T, Collection<BugInstance>>();
        for (Map.Entry<T, Collection<BugInstance>> entry : originalMap.entrySet()) {
            filteredMap.put(entry.getKey(), new FilteredCollection<BugInstance>(entry.getValue()));
        }
        return filteredMap;
    }

    public long getBugCount() {
        reallyUpdateMaps();
        return bugCount;
    }

    public long getBugCount(boolean isFilterOn) {
        reallyUpdateMaps();
        if (isFilterOn) {
            return coreBugCount;
        }
        return bugCount;
    }

    public long getCodeDefectCountSum() {
        return getBugCount();
    }

    public long getCodeDefactCount(CodeDefectSeverity severity) {
        reallyUpdateMaps();
        if (CodeDefectSeverity.INFO == severity) {
            return infoBugCount;
        }
        if (CodeDefectSeverity.WARNING == severity) {
            return warningBugCount;
        }
        if (CodeDefectSeverity.ERROR == severity) {
            return errorBugCount;
        }

        return 0;
    }

    public abstract static class DisplayableKey<T extends DisplayableKey> implements Comparable<T> {

        public abstract String getDisplayName();

        public @Override final boolean equals(Object object) {
            if (object instanceof DisplayableKey) {
                return ((DisplayableKey) object).getDisplayName().equals(getDisplayName());
            }
            return false;
        }

        public @Override final int hashCode() {
            return getDisplayName().hashCode();
        }

        public final int compareTo(T object) {
            return getDisplayName().compareTo(object.getDisplayName());
        }
    }

    public static class StringKey extends DisplayableKey<StringKey> {

        private String displayName;

        public StringKey(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }

    public static class ClassKey extends DisplayableKey<ClassKey> {

        private final ClassAnnotation classAnnotation;
        private final FileObject fileObject;

        public ClassKey(ClassAnnotation classAnnotation, Project project) {
            this.classAnnotation = classAnnotation;
            this.fileObject = BugAnnotationProcessor.findFileObjectForAnnotatedClass(classAnnotation.getClassName(), project);
        }

        public String getDisplayName() {
            return this.classAnnotation.getClassName();
        }

        public FileObject getFileObject() {
            return fileObject;
        }
    }

    public static class PackageKey extends DisplayableKey<PackageKey> {

        private ClassAnnotation classAnnotation;

        public PackageKey(ClassAnnotation classAnnotation) {
            this.classAnnotation = classAnnotation;
        }

        public String getDisplayName() {
            return this.classAnnotation.getPackageName();
        }
    }

    public static class CategoryKey extends DisplayableKey<CategoryKey> {

        private BugPattern bugPattern;

        public CategoryKey(BugPattern bugPattern) {
            this.bugPattern = bugPattern;
        }

        public String getDisplayName() {
            return this.bugPattern.getCategory();
        }
    }

    private static class FilteredCollection<T extends BugInstance> extends AbstractCollection<T> {

        Collection<T> originalCollection;

        public FilteredCollection(Collection<T> originalCollection) {
            this.originalCollection = originalCollection;
        }

        @SuppressWarnings("DLS_DEAD_LOCAL_STORE")
        public int size() {
            int i = 0;
            for (BugInstance instance : this) {
                i++;
            }
            return i;
        }

        public Iterator<T> iterator() {
            final Iterator<T> origIterator = originalCollection.iterator();
            return new Iterator<T>() {

                private T nextObject;
                private boolean hasNext;

                public T next() {
                    if (hasNext) {
                        return nextObject;
                    }
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext() {
                    while (origIterator.hasNext()) {
                        nextObject = origIterator.next();
                        BugPattern bugPattern = nextObject.getBugPattern();
                        if (FiBuUtil.isBugPatternIssuedFromCore(bugPattern)) {
                            return (hasNext = true);
                        }
                    }
                    return (hasNext = false);
                }
            };
        }
    }
}
