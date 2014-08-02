package me.tatarka.socket.compile;

import me.tatarka.socket.compile.util.FormatUtils;
import me.tatarka.socket.compile.util.Objects;

public class View {
    public final String type;
    public final String id;
    public final String fieldName;
    public final boolean isAndroidId;

    private View(String type, String id, String fieldName, boolean isAndroidId) {
        this.type = type;
        this.id = id;
        this.fieldName = fieldName != null ? fieldName : FormatUtils.underscoreToLowerCamel(id);
        this.isAndroidId = isAndroidId;
    }

    public static Builder of(String type, String id) {
        return new Builder(type, id);
    }

    public static class Builder {
        private String type;
        private String id;
        private String fieldName;
        private boolean isAndroidId;

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

        public View build() {
            return new View(type, id, fieldName, isAndroidId);
        }
    }

    @Override
    public String toString() {
        return type + "(id: " + (isAndroidId ? "android:" : "") + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        View view = (View) o;
        return id.equals(view.id) && type.equals(view.type) && isAndroidId == view.isAndroidId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, id, isAndroidId);
    }
}
