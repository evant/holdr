package me.tatarka.holdr.compile;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.tatarka.holdr.compile.util.FileUtils;

/**
 * Created by evan on 8/23/14.
 */
public class Layouts implements Iterable<Layouts.Layout> {
    private Map<String, Layout> layouts = new HashMap<String, Layout>();

    public void add(File layoutFile, Collection<Ref> refs) {
        String layoutName = FileUtils.stripExtension(layoutFile.getName());
        Layout layout = layouts.get(layoutName);

        if (layout == null) {
            layout = new Layout(layoutName, layoutFile);
            layouts.put(layoutName, layout);
        }

        layout.addAll(refs);
    }

    @Override
    public Iterator<Layout> iterator() {
        return layouts.values().iterator();
    }

    public static class Layout {
        public final String name;
        public final File file;
        public final Map<String, Ref> refs;
        private boolean isFirstLayout = true;

        private Layout(String name, File file) {
            this.name = name;
            this.file = file;
            this.refs = new HashMap<String, Ref>();
        }

        public void addAll(Collection<Ref> refs) {
            // We need to make sure we update every ref currently in the map because if there is not
            // a matching new ref, we still need to mark it as nullable. Similarly, new refs that
            // are not already in the map need to be marked as nullable.
            
            Map<String, Ref> newRefs = new HashMap<String, Ref>();
            for (Ref newRef : refs) {
                newRefs.put(newRef.getKey(), newRef);
            }
            
            if (isFirstLayout) {
                // The first time through, no merging is needed
                isFirstLayout = false;
                this.refs.putAll(newRefs);
            } else {
                // Update all current refs, merging in new ones
                for (Map.Entry<String, Ref> entry : this.refs.entrySet()) {
                    String key = entry.getKey();
                    Ref oldRef = entry.getValue();
                    Ref newRef = newRefs.remove(key);
                    this.refs.put(key, Ref.merge(name, oldRef, newRef));
                }
                // Merge in the remaining new ones
                for (Map.Entry<String, Ref> entry : newRefs.entrySet()) {
                    String key = entry.getKey();
                    Ref newRef = entry.getValue();
                    this.refs.put(key, Ref.merge(name, null, newRef));
                }
            }
        }

        public boolean isEmpty() {
            return refs.isEmpty();
        }
    }
}
