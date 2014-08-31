package me.tatarka.holdr.compile;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import me.tatarka.holdr.compile.util.FormatUtils;
import me.tatarka.holdr.compile.util.Objects;

public abstract class Ref {
    public final String id;
    public final String fieldName;
    public final boolean isAndroidId;
    public final boolean isNullable;

    protected Ref(String id, String fieldName, boolean isAndroidId, boolean isNullable) {
        this.id = id;
        this.fieldName = fieldName != null ? fieldName : FormatUtils.underscoreToLowerCamel(id);
        this.isAndroidId = isAndroidId;
        this.isNullable = isNullable;
    }

    public String getKey() {
        return (isAndroidId ? "android:" : "") + id;
    }

    public static Ref merge(String layoutName, @Nullable Ref oldRef, @Nullable Ref newRef) {
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
            View firstView = (View) oldRef;
            View secondView = (View) newRef;

            if (firstView.type.equals(secondView.type)) {
                return firstView;
            } else {
                return View.of("android.view.View", firstView).build();
            }
        }

        // Includes must have the same layout.
        if (oldRef instanceof Include && newRef instanceof Include) {
            String firstLayout = ((Include) oldRef).layout;
            String secondLayout = ((Include) newRef).layout;
            if (firstLayout.equals(secondLayout)) {
                return oldRef;
            } else {
                throw new IllegalArgumentException("Cannot merge includes with different layouts ('" + firstLayout + "', vs '" + secondLayout + "' in layout '" + layoutName + "').");
            }
        }

        throw new IllegalArgumentException("Cannot merge view with include (id '" + oldRef.id + "' in layout '" + layoutName + "').");
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
        StringBuilder b = new StringBuilder(toStringName());
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
