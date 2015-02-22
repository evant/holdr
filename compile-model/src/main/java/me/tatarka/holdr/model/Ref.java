package me.tatarka.holdr.model;

import me.tatarka.holdr.util.FormatUtils;
import me.tatarka.holdr.util.Objects;

import java.util.HashMap;
import java.util.Map;

public abstract class Ref {
    public final String id;
    public final String fieldName;
    public final boolean isAndroidId;
    public final boolean isNullable;
    public final boolean isFieldNameCustom;

    protected Ref(String id, boolean isAndroidId, String fieldName, boolean isNullable) {
        this.id = id;
        if (fieldName == null) {
            this.fieldName = FormatUtils.underscoreToLowerCamel(id);
            this.isFieldNameCustom = false;
        } else {
            this.fieldName = fieldName;
            this.isFieldNameCustom = true;
        }
        this.isAndroidId = isAndroidId;
        this.isNullable = isNullable;
    }

    public String getKey() {
        return (isAndroidId ? "android:" : "") + id;
    }

    public static Ref merge(String layoutName, Ref oldRef, Ref newRef) {
        if (oldRef == null && newRef == null) {
            throw new IllegalArgumentException("At least one of the refs to merge must not be null");
        }

        // One of the ref's doesn't exist, mark as nullable
        if (oldRef == null || newRef == null) {
            Ref ref = oldRef != null ? oldRef : newRef;
            if (ref instanceof View) {
                View view = (View) ref;
                return View.of(view.type, view).nullable().build();
            } else {
                Include include = (Include) ref;
                return Include.of(include).nullable().build();
            }
        }

        // Views don't need to be the same type, if not, just use android.view.View.
        if (oldRef instanceof View && newRef instanceof View) {
            View oldView = (View) oldRef;
            View newView = (View) newRef;
            return mergeViews(layoutName, oldView, newView);
        }

        // Includes must have the same layout.
        if (oldRef instanceof Include && newRef instanceof Include) {
            String firstLayout = ((Include) oldRef).layout;
            String secondLayout = ((Include) newRef).layout;
            if (firstLayout.equals(secondLayout)) {
                return oldRef;
            } else {
                throw new IllegalArgumentException("Cannot merge includes with different layouts ('" + firstLayout + "' vs '" + secondLayout + "' in layout '" + layoutName + "').");
            }
        }

        throw new IllegalArgumentException("Cannot merge view with include (id '" + oldRef.id + "' in layout '" + layoutName + "').");
    }

    private static View mergeViews(String layoutName, View oldView, View newView) {
        String type = oldView.type.equals(newView.type) ? oldView.type : "android.view.View";
        View.Builder view = View.of(type, oldView);
        view.fieldName(mergeFieldNames(layoutName, oldView, newView));
        return view.build();
    }

    private static String mergeFieldNames(String layoutName, Ref oldRef, Ref newRef) {
        if (oldRef.isFieldNameCustom || newRef.isFieldNameCustom) {
            if (oldRef.isFieldNameCustom && newRef.isFieldNameCustom) {
                if (oldRef.fieldName.equals(newRef.fieldName)) {
                    return oldRef.fieldName;
                } else {
                    throw new IllegalArgumentException("Cannot merge views with different view names ('" + oldRef.fieldName + "' vs '" + newRef.fieldName + "' in layout '" + layoutName + "').");
                }
            }
            return oldRef.isFieldNameCustom ? oldRef.fieldName : newRef.fieldName;
        }
        return oldRef.fieldName;
    }

    public static abstract class Builder<R extends Ref, T extends Builder> {
        protected String id;
        protected String fieldName;
        protected boolean isAndroidId;
        protected boolean isNullable;

        protected Builder(String id) {
            if (id == null) throw new IllegalStateException("id must not be null");
            this.id = id;
        }

        protected Builder(Ref ref) {
            this(ref.id);
            fieldName = ref.fieldName;
            isAndroidId = ref.isAndroidId;
        }

        public T fieldName(String fieldName) {
            this.fieldName = fieldName;
            return (T) this;
        }

        public T androidId() {
            isAndroidId = true;
            return (T) this;
        }

        public T nullable() {
            isNullable = true;
            return (T) this;
        }

        public abstract R build();
    }

    protected abstract String toStringName();

    protected Map<String, String> toStringFields() {
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("id", (isAndroidId ? "android:" : "") + id);
        return fields;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(toStringName()).append(":").append(fieldName);
        if (isNullable) b.append("?");
        b.append("(");
        boolean isFirst = true;
        for (Map.Entry<String, String> fieldEntry : toStringFields().entrySet()) {
            if (!isFirst) {
                b.append(", ");
            }
            b.append(fieldEntry.getKey()).append(": ").append(fieldEntry.getValue());
            isFirst = false;
        }
        b.append(")");
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ref ref = (Ref) o;

        return id.equals(ref.id)
                && fieldName.equals(ref.fieldName)
                && isAndroidId == ref.isAndroidId
                && isNullable == ref.isNullable;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, fieldName, isAndroidId, isNullable);
    }
}
