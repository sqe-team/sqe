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
import java.util.Collection;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.nbheaven.sqe.codedefects.core.api.QualityResult;
import org.nbheaven.sqe.codedefects.core.api.QualityResultStatistic;
import org.nbheaven.sqe.tools.findbugs.codedefects.core.annotations.BugAnnotationProcessor;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Sven Reimers
 */
public interface FindBugsResult extends QualityResult, QualityResultStatistic {

    public enum Mode {

        CLASS("HINT_VIEW_BY_CLASS", "org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/class.gif") {

            @Override
            public Map<?, Collection<BugInstance>> getInstanceList(final FindBugsResult result, boolean coreBugsOnly) {
                return result.getInstanceByClass(coreBugsOnly);
            }
        },
        PACKAGE("HINT_VIEW_BY_PACKAGE", "org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/package.gif") {

            @Override
            public Map<?, Collection<BugInstance>> getInstanceList(final FindBugsResult result, boolean coreBugsOnly) {
                return result.getInstanceByPackage(coreBugsOnly);
            }
        },
        CATEGORY("HINT_VIEW_BY_CATEGORY", "org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/bug.png") {

            @Override
            public Map<?, Collection<BugInstance>> getInstanceList(final FindBugsResult result, boolean coreBugsOnly) {
                return result.getInstanceByCategory(coreBugsOnly);
            }
        },
        TYPE("HINT_VIEW_BY_CATEGORY", "org/nbheaven/sqe/tools/findbugs/codedefects/core/resources/bug2.png") {

            @Override
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

    public void removeAllBugInstancesForBugPattern(BugPattern bugPattern);

    public Map<ClassKey, Collection<BugInstance>> getInstanceByClass(boolean coreBugsOnly);

    public Map<PackageKey, Collection<BugInstance>> getInstanceByPackage(boolean coreBugsOnly);

    public Map<CategoryKey, Collection<BugInstance>> getInstanceByCategory(boolean coreBugsOnly);

    public Map<BugPattern, Collection<BugInstance>> getInstanceByType(boolean coreBugsOnly);

    public long getCodeDefectCount(boolean isFilterOn);

    @Override
    public long getCodeDefectCountSum();

    public abstract static class DisplayableKey<T extends DisplayableKey<?>> implements Comparable<T> {

        public abstract String getDisplayName();

        @Override
        public final boolean equals(Object object) {
            if (object instanceof DisplayableKey) {
                return ((DisplayableKey) object).getDisplayName().equals(getDisplayName());
            }
            return false;
        }

        public @Override
        final int hashCode() {
            return getDisplayName().hashCode();
        }

        @Override
        public final int compareTo(T object) {
            return getDisplayName().compareTo(object.getDisplayName());
        }
    }

    public static class StringKey extends DisplayableKey<StringKey> {

        private final String displayName;

        public StringKey(String displayName) {
            this.displayName = displayName;
        }

        @Override
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

        @Override
        public String getDisplayName() {
            return this.classAnnotation.getClassName();
        }

        public FileObject getFileObject() {
            return fileObject;
        }
    }

    public static class PackageKey extends DisplayableKey<PackageKey> {

        private final ClassAnnotation classAnnotation;

        public PackageKey(ClassAnnotation classAnnotation) {
            this.classAnnotation = classAnnotation;
        }

        @Override
        public String getDisplayName() {
            return this.classAnnotation.getPackageName();
        }
    }

    public static class CategoryKey extends DisplayableKey<CategoryKey> {

        private final BugPattern bugPattern;

        public CategoryKey(BugPattern bugPattern) {
            this.bugPattern = bugPattern;
        }

        @Override
        public String getDisplayName() {
            return this.bugPattern.getCategory();
        }
    }

//    private static class FilteredCollection<T extends BugInstance> extends AbstractCollection<T> {
//
//        Collection<T> originalCollection;
//
//        public FilteredCollection(Collection<T> originalCollection) {
//            this.originalCollection = originalCollection;
//        }
//
//        @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
//        @Override
//        public int size() {
//            int i = 0;
//            for (BugInstance instance : this) {
//                i++;
//            }
//            return i;
//        }
//
//        @Override
//        public Iterator<T> iterator() {
//            final Iterator<T> origIterator = originalCollection.iterator();
//            return new Iterator<T>() {
//
//                private T nextObject;
//                private boolean hasNext;
//
//                @Override
//                public T next() {
//                    if (hasNext) {
//                        return nextObject;
//                    }
//                    throw new NoSuchElementException();
//                }
//
//                @Override
//                public void remove() {
//                    throw new UnsupportedOperationException();
//                }
//
//                @Override
//                public boolean hasNext() {
//                    while (origIterator.hasNext()) {
//                        nextObject = origIterator.next();
//                        BugPattern bugPattern = nextObject.getBugPattern();
//                        if (FiBuUtil.isBugPatternIssuedFromCore(bugPattern)) {
//                            return (hasNext = true);
//                        }
//                    }
//                    return (hasNext = false);
//                }
//            };
//        }
//    }
}
