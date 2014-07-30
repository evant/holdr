package me.tatarka.socket.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.tatarka.socket.compile.util.FormatUtils;
import me.tatarka.socket.compile.util.Objects;

public class View {
    public final String type;
    public final String id;
    public final String fieldName;
    public final boolean isAndroidId;
    public final List<View> children;

    private View(String type, String id, String fieldName, boolean isAndroidId, List<View> children) {
        this.type = type;
        this.id = id;
        this.fieldName = fieldName != null ? fieldName : FormatUtils.underscoreToLowerCamel(id);
        this.isAndroidId = isAndroidId;
        this.children = Collections.unmodifiableList(children);
    }

    public static Builder of(String type, String id) {
        return new Builder(type, id);
    }

    public static class Builder {
        private Builder parent;
        private String type;
        private String id;
        private String fieldName;
        private boolean isAndroidId;
        private List<View> children = new ArrayList<View>();

        private Builder(String type, String id) {
            if (type == null) throw new IllegalStateException("type must not be null");
            if (id == null) throw new IllegalStateException("id must not be null");

            this.type = type;
            this.id = id;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder androidId() {
            isAndroidId = true;
            return this;
        }

        public Builder parent(Builder parent) {
            this.parent = parent;
            return this;
        }

        public Builder parent() {
            return parent;
        }

        public Builder child(Builder view) {
            children.add(view.parent(this).build());
            return this;
        }

        public View build() {
            return new View(type, id, fieldName, isAndroidId, children);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(type + "(id: " + (isAndroidId ? "android:" : "") + id);

        if (!children.isEmpty()) {
            result.append(", children: ");
            for (int i = 0; i < children.size(); i++) {
                View child = children.get(i);
                result.append(child.toString());
                if (i != children.size() - 1) result.append(", ");
            }
        }
        result.append(")");

        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        View view = (View) o;
        return children.equals(view.children) && id.equals(view.id) && type.equals(view.type) && isAndroidId == view.isAndroidId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, id, isAndroidId, children);
    }
}
