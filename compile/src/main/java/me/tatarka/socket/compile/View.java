package me.tatarka.socket.compile;


public class View extends Ref {
    public final String type;

    private View(String type, String id, String fieldName, boolean isAndroidId) {
        super(id, fieldName, isAndroidId);
        this.type = type;
    }

    public static Builder of(String type, String id) {
        return new Builder(type, id);
    }

    public static class Builder extends Ref.Builder<View, Builder> {
        private String type;
        private String id;

        private Builder(String type, String id) {
            super(id);
            if (type == null) throw new IllegalStateException("type must not be null");

            this.type = type;
            this.id = id;
        }

        @Override
        public View build() {
            return new View(type, id, fieldName, isAndroidId);
        }
    }

    @Override
    protected String toStringName() {
        return type;
    }
}
