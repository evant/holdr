package me.tatarka.socket.compile;

import java.util.Map;

public class Include extends Ref {
    public final String layout;

    private Include(String layout, String id, String fieldName, boolean isAndroidId) {
        super(id, fieldName, isAndroidId);
        this.layout = layout;
    }

    public static Builder of(String layout, String id) {
        return new Builder(layout, id);
    }

    public static class Builder extends Ref.Builder<Include, Builder> {
        private String layout;

        private Builder(String layout, String id) {
            super(id);
            this.layout = layout;
        }

        @Override
        public Include build() {
            return new Include(layout, id, fieldName, isAndroidId);
        }
    }

    @Override
    protected String toStringName() {
        return "include";
    }

    @Override
    protected Map<String, String> toStringFields() {
        Map<String, String> fields = super.toStringFields();
        fields.put("layout", layout);
        return fields;
    }
}
