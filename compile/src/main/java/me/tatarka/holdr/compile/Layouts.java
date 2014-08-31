package me.tatarka.holdr.compile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.tatarka.holdr.compile.model.Ref;
import me.tatarka.holdr.compile.util.FileUtils;

/**
 * Created by evan on 8/23/14.
 */
public class Layouts implements Iterable<Layouts.Layout> {
    private Map<String, Layout> layouts = new HashMap<String, Layout>();

    public void add(File layoutFile, ParsedLayout parsedLayout) {
        String layoutName = FileUtils.stripExtension(layoutFile.getName());
        Layout layout = layouts.get(layoutName);

        if (layout == null) {
            layout = new Layout(layoutName, layoutFile);
            layouts.put(layoutName, layout);
        }

        layout.add(parsedLayout);
    }

    @Override
    public Iterator<Layout> iterator() {
        return layouts.values().iterator();
    }

    public static class Layout {
        public final String name;
        public final File file;
        public final List<Ref> refs;
        
        private boolean isFirstLayout = true;
        private String superclass = null;

        private Layout(String name, File file) {
            this.name = name;
            this.file = file;
            this.refs = new ArrayList<Ref>();
        }
        
        public String getSuperclass() {
            return superclass != null ? superclass : HoldrGenerator.HOLDR_SUPERCLASS;
        }
        
        public void add(ParsedLayout layout) {
            if (layout.superclass != null) {
                if (superclass == null) {
                    superclass = layout.superclass;
                } else if (!superclass.equals(layout.superclass)) {
                    throw new IllegalArgumentException("layout '" + name + "' has conflicting superclasses ('" + superclass + "' vs '" + layout.superclass + "'.");
                }
            }
            addAllRefs(layout.refs); 
        }

        private void addAllRefs(List<Ref> newRefs) {
            // We need to make sure we update every ref currently in the map because if there is not
            // a matching new ref, we still need to mark it as nullable. Similarly, new refs that
            // are not already in the map need to be marked as nullable.
            
            if (isFirstLayout) {
                // The first time through, no merging is needed
                isFirstLayout = false;
                this.refs.addAll(newRefs);
            } else {
                // Update all current refs, merging in new ones
                for (int i = 0; i < this.refs.size(); i++) {
                    Ref oldRef = this.refs.get(i);
                    Ref newRef = remove(oldRef.getKey(), newRefs);
                    this.refs.set(i, Ref.merge(name, oldRef, newRef));
                }
                
                // Merge in the remaining new ones
                for (Ref newRef : newRefs) {
                    this.refs.add(Ref.merge(name, null, newRef));
                }
            }
        }
        
        private static Ref remove(String key, List<Ref> refs) {
            for (int i = 0; i < refs.size(); i++) {
                Ref ref = refs.get(i);
                if (ref.getKey().equals(key)) {
                    return refs.remove(i);
                }
            }
            return null;
        }
        
        public boolean isEmpty() {
            return refs.isEmpty();
        }
    }
}
