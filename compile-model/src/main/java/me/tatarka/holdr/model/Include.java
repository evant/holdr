package me.tatarka.holdr.model;

import java.util.Map;

import me.tatarka.holdr.util.Objects;

public class Include extends Ref {
    public final String layout;

    private Include(String id, boolean isAndroidId, String fieldName, boolean isNullable, String layout) {
        super(id, isAndroidId, fieldName, isNullable);
        this.layout = layout;
    }

    public static Builder of(String layout, String id) {
        return new Builder(layout, id);
    }
    
    public static Builder of(Include include) {
        return new Builder(include);
    }

    public static class Builder extends Ref.Builder<Include, Builder> {
        private String layout;

        private Builder(String layout, String id) {
            super(id);
            this.layout = layout;
        }
        
        private Builder(Include include) {
            super(include);
            this.layout = include.layout;
        }

        @Override
        public Include build() {
            return new Include(id, isAndroidId, fieldName, isNullable, layout);
        }
    }

    @Override
    protected String toStringName() {
        return "include";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Include include = (Include) o;

        return id.equals(include.id)
                && isAndroidId == include.isAndroidId
                && fieldName.equals(include.fieldName)
                && isNullable == include.isNullable
                && layout.equals(include.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, isAndroidId, fieldName, isNullable, layout);
    }

    @Override
    protected Map<String, String> toStringFields() {
        Map<String, String> fields = super.toStringFields();
        fields.put("layout", layout);
        return fields;
    }
}
