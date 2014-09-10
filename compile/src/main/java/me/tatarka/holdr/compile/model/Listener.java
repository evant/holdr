package me.tatarka.holdr.compile.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.tatarka.holdr.compile.util.FormatUtils;
import me.tatarka.holdr.compile.util.Objects;

/**
 * Created by evan on 8/31/14.
 */
public class Listener {
    @NotNull
    public final Type type;
    @NotNull
    public final String name;
    @NotNull
    public final String viewType;
    @NotNull
    public final String viewName;

    private Listener(@NotNull Type type, @NotNull String name, @NotNull String viewType, @NotNull String viewName) {
        this.type = type;
        this.name = name;
        this.viewType = viewType;
        this.viewName = viewName;
    }

    public static Builder of(Type type) {
        return new Builder(type);
    }

    public static Builder of(Listener listener) {
        return new Builder(listener);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Listener listener = (Listener) o;

        return type == listener.type && name.equals(listener.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, name);
    }

    public static class Builder {
        @NotNull
        private Type type;
        @Nullable
        private String name;

        private Builder(@NotNull Type type) {
            this.type = type;
        }
        
        private Builder(Listener listener) {
            this.type = listener.type;
            this.name = listener.name;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Listener build(@NotNull String fieldName, @NotNull String viewType, @NotNull String viewName) {
            if (name == null) name = nameFromView(type, fieldName);
            return new Listener(type, name, viewType, viewName);
        }

        private static String nameFromView(Listener.Type type, String fieldName) {
            return "on" + FormatUtils.capiatalize(fieldName) + type.nameSuffix();
        }
    }

    public static enum Type {
        ON_TOUCH("Touch"),
        ON_CLICK("Click"), ON_LONG_CLICK("LongClick"),
        ON_FOCUS_CHANGE("FocusChange"), ON_CHECKED_CHANGE("CheckedChanged"), ON_EDITOR_ACTION("EditorAction"),
        ON_ITEM_CLICK("ItemClick"), ON_ITEM_LONG_CLICK("ItemLongClick");
        
        public final String name;
        
        Type(String name) {
            this.name = name;
        }

        public String nameSuffix() {
            return name;
        }

        public String layoutName() {
            return "on" + name;
        }
    }
}
