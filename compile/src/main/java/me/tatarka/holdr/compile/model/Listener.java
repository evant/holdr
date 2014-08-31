package me.tatarka.holdr.compile.model;

import org.jetbrains.annotations.NotNull;

import me.tatarka.holdr.compile.util.FormatUtils;
import me.tatarka.holdr.compile.util.Objects;

/**
 * Created by evan on 8/31/14.
 */
public class Listener {
    public final Type type;
    public final String name;

    private Listener(Type type, String name, View view) {
        this.type = type;
        this.name = name != null ? name : nameFromView(type, view);
    }

    public static Builder of(Type type) {
        return new Builder(type);
    }

    private static String nameFromView(Type type, View view) {
        return "on" + FormatUtils.capiatalize(view.fieldName) + type.nameSuffix();
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
        private Type type;
        private String name;

        private Builder(@NotNull Type type) {
            this.type = type;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Listener build(View view) {
            return new Listener(type, name, view);
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
