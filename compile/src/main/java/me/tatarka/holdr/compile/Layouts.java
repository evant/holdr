package me.tatarka.holdr.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.tatarka.holdr.compile.model.Include;

/**
 * Created by evan on 8/23/14.
 */
public class Layouts {
    private Map<String, Layout.Builder> layoutBuilders = new HashMap<String, Layout.Builder>();
    private Map<String, Layout> layouts = new HashMap<String, Layout>();

    public static Layouts of(Layout.Builder... layoutBuilders) {
        return Layouts.of(Arrays.asList(layoutBuilders));
    }

    public static Layouts of(Iterable<Layout.Builder> layoutBuilders) {
        Layouts layouts = new Layouts();
        for (Layout.Builder builder : layoutBuilders) {
            layouts.add(builder);
        }
        return layouts;
    }
    
    public IncludeResolver asIncludeResolver() {
        return includeResolver;
    }

    public void add(Layout.Builder layoutBuilder) {
        Layout.Builder oldLayoutBuilder = layoutBuilders.get(layoutBuilder.name);
        if (oldLayoutBuilder == null) {
            layoutBuilders.put(layoutBuilder.name, layoutBuilder);
        } else {
            oldLayoutBuilder.merge(layoutBuilder);
        }
    }

    public Set<String> getNames() {
        return layoutBuilders.keySet();
    }

    public Layout get(String layoutName) {
        Layout layout = layouts.get(layoutName);
        if (layout == null) {
            layout = layoutBuilders.get(layoutName).build();
            layouts.put(layoutName, layout);
        }
        return layout;
    }
    
    public boolean contains(String layoutName) {
        return layoutBuilders.get(layoutName) != null;
    }
    
    public List<String> getExtraIncludes() {
        List<String> includes = new ArrayList<String>();
        for (Layout.Builder builder : layoutBuilders.values()) {
            for (String include : builder.getIncludes()) {
                if (!contains(include)) {
                   includes.add(include); 
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
