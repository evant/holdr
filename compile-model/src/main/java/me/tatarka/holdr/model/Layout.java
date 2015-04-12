package me.tatarka.holdr.model;

import me.tatarka.holdr.util.Objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by evan on 8/30/14.
 */
public abstract class Layout {
    public static Builder of(File path) {
        return new Builder(path);
    }
    
    public static CompositeLayout merge(SingleLayout.Builder... layouts) {
        return merge(Arrays.asList(layouts));
    }

    public static CompositeLayout merge(Iterable<SingleLayout.Builder> layouts) {
        CompositeLayout compositeLayout = new CompositeLayout();
        for (Layout.Builder layout : layouts) {
            compositeLayout.put(layout.build());
        }
        return compositeLayout;
    }

    public boolean isEmpty() {
        return getRefs().isEmpty();
    }

    public int size() {
        return getRefs().size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Layout that = (Layout) o;

        return getLayoutInfo().equals(that.getLayoutInfo())
                && getRefs().equals(that.getRefs())
                && getListeners().equals(that.getListeners());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLayoutInfo(), getRefs(), getListeners());
    }

    public String getName() {
        return getLayoutInfo().getName();
    }

    public String getSuperclass() {
        return getLayoutInfo().getSuperclass();
    }

    public boolean isRootMerge() {
        return getLayoutInfo().isRootMerge();
    }

    public Ref findRefByFieldName(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        for (Ref ref : getRefs()) {
            if (fieldName.equals(ref.fieldName)) {
                return ref;
            }
        }
        return null;
    }

    public List<Include> getIncludes() {
        List<Include> includes = new ArrayList<Include>();
        for (Ref ref : getRefs()) {
            if (ref instanceof Include) {
                includes.add((Include) ref);
            }
        }
        return includes;
    }
    
    public abstract LayoutInfo getLayoutInfo();
    public abstract List<Ref> getRefs();
    public abstract Listeners getListeners();

    public static class Builder {
        public final File path;
        private String superclass;
        private boolean isRootMerge;
        private List<Ref> refs = new ArrayList<Ref>();
        private Listeners.Builder listenersBuilder = Listeners.of();

        private Builder(File path) {
            this.path = path;
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

        public Builder listeners(Listeners.Builder listenersBuilder) {
            this.listenersBuilder = listenersBuilder;
            return this;
        }
        
        public Builder superclass(String superclass) {
            this.superclass = superclass;
            return this;
        }

        public Builder rootMerge(boolean isRootMerge) {
            this.isRootMerge = isRootMerge;
            return this;
        }

        public SingleLayout build() {
            return new SingleLayout(path, superclass, isRootMerge, refs, listenersBuilder.build());
        }
    }
}
