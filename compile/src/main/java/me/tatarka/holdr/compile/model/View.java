package me.tatarka.holdr.compile.model;


import java.util.ArrayList;
import java.util.List;

import me.tatarka.holdr.compile.util.Objects;

public class View extends Ref {
    public final String type;

    private View(String id, boolean isAndroidId, String fieldName, boolean isNullable, String type) {
        super(id, isAndroidId, fieldName, isNullable);
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
        private List<Listener.Builder> listenerBuilders = new ArrayList<Listener.Builder>();

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
        
        public Builder listener(Listener.Builder listenerBuilder) {
            listenerBuilders.add(listenerBuilder);
            return this;
        }

        public Builder listener(Listener.Type listenerType) {
            return listener(Listener.of(listenerType));
        }

        @Override
        public View build() {
            return new View(id, isAndroidId, fieldName, isNullable, type);
        }
        
        public List<Listener.Builder> getListenerBuilders() {
            return listenerBuilders;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Builder builder = (Builder) o;

            return id.equals(builder.id)
                    && isAndroidId == builder.isAndroidId
                    && type.equals(builder.type)
                    && listenerBuilders.equals(builder.listenerBuilders);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, isAndroidId, type, listenerBuilders);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        View view = (View) o;

        return id.equals(view.id)
                && isAndroidId == view.isAndroidId
                && fieldName.equals(view.fieldName)
                && isNullable == view.isNullable
                && type.equals(view.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, isAndroidId, fieldName, isNullable, type);
    }

    @Override
    protected String toStringName() {
        return type;
    }
}
