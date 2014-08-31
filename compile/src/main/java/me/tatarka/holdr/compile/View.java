package me.tatarka.holdr.compile;


import me.tatarka.holdr.compile.util.Objects;

public class View extends Ref {
    public final String type;

    private View(String type, String id, String fieldName, boolean isAndroidId, boolean isNullable) {
        super(id, fieldName, isAndroidId, isNullable);
        this.type = type;
    }

    public static Builder of(String type, String id) {
        return new Builder(type, id);
    }
    
    public static Builder of(String type, View view) {
        return new Builder(type, view);
    }
    
    public static class Builder extends Ref.Builder<View, Builder> {
        private String type;

        private Builder(String type, String id) {
            super(id);
            if (type == null) throw new IllegalStateException("type must not be null");

            this.type = type;
        }

        private Builder(String type, View view) {
            super(view);
            if (type == null) throw new IllegalStateException("type must not be null");
            
            this.type = type;
        }

        @Override
        public View build() {
            return new View(type, id, fieldName, isAndroidId, isNullable);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Builder builder = (Builder) o;

            if (!type.equals(builder.type)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, isAndroidId, type);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        View view = (View) o;

        return id.equals(view.id)
                && fieldName.equals(view.fieldName)
                && isAndroidId == view.isAndroidId
                && isNullable == view.isNullable
                && type.equals(view.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, fieldName, isAndroidId, isNullable, type);
    }

    @Override
    protected String toStringName() {
        return type;
    }
}
