package me.tatarka.holdr.compile;

import me.tatarka.holdr.model.CompositeLayout;
import me.tatarka.holdr.model.Include;
import me.tatarka.holdr.model.Layout;
import me.tatarka.holdr.model.SingleLayout;

import java.util.*;

/**
 * Created by evan on 8/23/14.
 */
public class Layouts {
    private Map<String, CompositeLayout> layouts = new HashMap<String, CompositeLayout>();

    public static Layouts of(Layout.Builder... layoutBuilds) {
        return of(Arrays.asList(layoutBuilds));
    }

    public static Layouts of(Iterable<Layout.Builder> layoutBuilders) {
        Layouts result = new Layouts();
        for (Layout.Builder layoutBuilder : layoutBuilders) {
            CompositeLayout layout = CompositeLayout.of(layoutBuilder);
            result.layouts.put(layout.getName(), layout);
        }
        return result;
    }

    public IncludeResolver asIncludeResolver() {
        return includeResolver;
    }

    public void put(SingleLayout layout) {
        internalGet(layout.getName()).put(layout);
    }

    public Set<String> getNames() {
        return layouts.keySet();
    }

    public Layout get(String name) {
        return internalGet(name);
    }

    public CompositeLayout internalGet(String name) {
        CompositeLayout layout = layouts.get(name);
        if (layout == null) {
            layout = new CompositeLayout();
            layouts.put(name, layout);
        }
        return layout;
    }

    public boolean contains(String name) {
        return !get(name).isEmpty();
    }

    public List<String> getExtraIncludes() {
        List<String> includes = new ArrayList<String>();
        for (Layout layout : layouts.values()) {
            for (Include include : layout.getIncludes()) {
                if (!contains(include.layout)) {
                    includes.add(include.layout);
                }
            }
        }
        return includes;
    }

    private final IncludeResolver includeResolver = new IncludeResolver() {
        @Override
        public Layout resolveInclude(Include include) {
            return get(include.layout);
        }
    };
}
