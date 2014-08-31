package me.tatarka.holdr.compile;

import java.util.Map;

import me.tatarka.holdr.compile.util.Objects;

public class Include extends Ref {
    public final String layout;

    private Include(String layout, String id, String fieldName, boolean isAndroidId, boolean isNullable) {
        super(id, fieldName, isAndroidId, isNullable);
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
            return new Include(layout, id, fieldName, isAndroidId, isNullable);
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
                && fieldName.equals(include.fieldName)
                && isAndroidId == include.isAndroidId
                && isNullable == include.isNullable
                && layout.equals(include.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, fieldName, isAndroidId, isNullable, layout);
    }

    @Override
    protected Map<String, String> toStringFields() {
        Map<String, String> fields = super.toStringFields();
        fields.put("layout", layout);
        return fields;
    }
}
