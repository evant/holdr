package me.tatarka.holdr.compile;

import me.tatarka.holdr.compile.model.Include;
import me.tatarka.holdr.compile.model.Listeners;
import me.tatarka.holdr.compile.model.Ref;
import me.tatarka.holdr.compile.model.View;
import me.tatarka.holdr.compile.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evan on 8/30/14.
 */
public class Layout {
    @NotNull
    public final String name;
    @NotNull
    public final String superclass;
    @NotNull
    public final List<Ref> refs;
    @NotNull
    public final Listeners listeners;

    private Layout(@NotNull String name, @NotNull String superclass, @NotNull List<Ref> refs, @NotNull Listeners listeners) {
        this.name = name;
        this.superclass = superclass;
        this.refs = refs;
        this.listeners = listeners;
    }

    public static Builder of(String name) {
        return new Builder(name);
    }
    
    @Nullable
    public static Layout merge(Builder... builders) {
        if (builders.length == 0) {
            return null;
        }
        Builder first = builders[0];
        for (int i = 1; i < builders.length; i++) {
            Builder builder = builders[i];
            first.merge(builder);
        }
        return first.build();
    }
    
    public boolean isEmpty() {
        return refs.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Layout that = (Layout) o;

        return name.equals(that.name) 
                && superclass.equals(that.superclass)
                && refs.equals(that.refs)
                && listeners.equals(that.listeners);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, superclass, refs, listeners);
    }

    public static class Builder {
        public final String name;
        private String superclass;
        private List<Ref> refs = new ArrayList<Ref>();
        private Listeners.Builder listenersBuilder = Listeners.of();
        
        private Builder(String name) {
            this.name = name;
        }
        
        public Builder include(Include.Builder includeBuilder) {
            refs.add(includeBuilder.build());
            return this;
        }
        
        public Builder view(View.Builder viewBuilder) {
            View view = viewBuilder.build();
            refs.add(view);
            listenersBuilder.add(view, viewBuilder.getListenerBuilders());
            return this;
        }

        public Builder superclass(String superclass) {
            this.superclass = superclass;
            return this;
        }

        public Layout build() {
            if (superclass == null) superclass = HoldrGenerator.HOLDR_SUPERCLASS;
            return new Layout(name, superclass, refs, listenersBuilder.build());
        }

        public Builder merge(Builder other) {
            if (this == other) {
                throw new IllegalArgumentException("Can't merge with self!");
            }

            if (!name.equals(other.name)) {
                throw new IllegalArgumentException("Can't merge different layouts ('" + name + "' and '" + other.name + "').");
            }
            
            mergeSuperclass(other.superclass); 
            mergeRefs(other.refs);
            return this;
        }
        
        private void mergeSuperclass(String otherSuperclass) {
            if (otherSuperclass != null) {
                if (superclass == null) {
                    superclass = otherSuperclass;
                } else if (!superclass.equals(otherSuperclass)) {
                    throw new IllegalArgumentException("layout '" + name + "' has conflicting superclasses ('" + superclass + "' vs '" + otherSuperclass + "').");
                }
            }
        }
        
        private void mergeRefs(List<Ref> newRefs) {
            // We need to make sure we update every ref currently in the map because if there is not
            // a matching new ref, we still need to mark it as nullable. Similarly, new refs that
            // are not already in the map need to be marked as nullable.

            // Update all current refs, merging in new ones
            for (int i = 0; i < refs.size(); i++) {
                Ref oldRef = refs.get(i);
                Ref newRef = removeRef(oldRef.getKey(), newRefs);
                refs.set(i, Ref.merge(name, oldRef, newRef));
            }

            // Merge in the remaining new ones
            for (Ref newRef : newRefs) {
                refs.add(Ref.merge(name, null, newRef));
            }
        }

        private static Ref removeRef(String key, List<Ref> refs) {
            for (int i = 0; i < refs.size(); i++) {
                Ref ref = refs.get(i);
                if (ref.getKey().equals(key)) {
                    return refs.remove(i);
                }
            }
            return null;
        }
    }
}
