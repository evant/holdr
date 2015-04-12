package me.tatarka.holdr.model;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by evan on 2/13/15.
 */
public class LayoutInfo {
    private static final String DEFAULT_SUPERCLASS = "me.tatarka.holdr.Holdr";

    private final String name;
    private final String superclass;
    private final boolean isRootMerge;

    LayoutInfo(String name, String superclass, boolean isRootMerge) {
        this.name = name;
        this.superclass = superclass != null ? superclass : DEFAULT_SUPERCLASS;
        this.isRootMerge = isRootMerge;
    }

    public String getName() {
        return name;
    }

    public String getSuperclass() {
        return superclass;
    }

    public boolean isRootMerge() {
        return isRootMerge;
    }

    public static LayoutInfo merge(Collection<LayoutInfo> layoutInfos) {
        if (layoutInfos.isEmpty()) {
            return null;
        }

        Iterator<LayoutInfo> iterator = layoutInfos.iterator();
        LayoutInfo first = iterator.next();

        String name = first.getName();
        String superclass = first.getSuperclass();
        boolean isRootMerge = first.isRootMerge();

        while (iterator.hasNext()) {
            LayoutInfo next = iterator.next();

            if (!name.equals(next.name)) {
                throw new IllegalArgumentException("Can't merge different layouts ('" + name + "' and '" + next.name + "').");
            }

            superclass = mergeSuperclass(name, superclass, next.getSuperclass());
            isRootMerge = mergeIsRootMerge(isRootMerge, next.isRootMerge());
        }

        return new LayoutInfo(name, superclass, isRootMerge);
    }

    private static String mergeSuperclass(String name, String superclass1, String superclass2) {
        if (!DEFAULT_SUPERCLASS.equals(superclass2)) {
            if (DEFAULT_SUPERCLASS.equals(superclass1)) {
                return superclass2;
            } else if (!superclass1.equals(superclass2)) {
                throw new IllegalArgumentException("layout '" + name + "' has conflicting superclasses ('" + superclass1 + "' vs '" + superclass2 + "').");
            }
        }
        return superclass1;
    }

    private static boolean mergeIsRootMerge(boolean isRootMerge1, boolean isRootMerge2) {
        return isRootMerge1 | isRootMerge2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LayoutInfo that = (LayoutInfo) o;

        if (isRootMerge != that.isRootMerge) return false;
        if (!name.equals(that.name)) return false;
        if (!superclass.equals(that.superclass)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + superclass.hashCode();
        result = 31 * result + (isRootMerge ? 1 : 0);
        return result;
    }
}
