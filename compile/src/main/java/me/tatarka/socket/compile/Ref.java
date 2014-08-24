package me.tatarka.socket.compile;

import java.util.HashMap;
import java.util.Map;

import me.tatarka.socket.compile.util.FormatUtils;
import me.tatarka.socket.compile.util.Objects;

public abstract class Ref {
    public final String id;
    public final String fieldName;
    public final boolean isAndroidId;

    protected Ref(String id, String fieldName, boolean isAndroidId) {
        this.id = id;
        this.fieldName = fieldName != null ? fieldName : FormatUtils.underscoreToLowerCamel(id);
        this.isAndroidId = isAndroidId;
    }
    
    public static Ref merge(String layoutName, Ref first, Ref second) {
        if (first instanceof View && second instanceof View) {
            View firstView = (View) first;
            View secondView = (View) second;
            
            if (firstView.type.equals(secondView.type)) {
                return firstView;
            } else {
                return View.of("android.view.View", firstView).build();
            }
        }
        
        if (first instanceof Include && second instanceof Include) {
            String firstLayout = ((Include) first).layout;
            String secondLayout = ((Include) second).layout;
            if (firstLayout.equals(secondLayout)) {
                return first;
            } else {
                throw new IllegalArgumentException("Cannot merge includes with different layouts ('" + firstLayout + "', vs '" + secondLayout + "' in layout '" + layoutName + "').");
            }
        }

        throw new IllegalArgumentException("Cannot merge view with include (id '" + first.id + "' in layout '" + layoutName + "').");
    }

    public static abstract class Builder<R extends Ref, T extends Builder> {
        protected String id;
        protected String fieldName;
        protected boolean isAndroidId;

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
        StringBuilder b = new StringBuilder(toStringName()).append("(");
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
        return id.equals(ref.id) && isAndroidId == ref.isAndroidId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, isAndroidId);
    }
}
