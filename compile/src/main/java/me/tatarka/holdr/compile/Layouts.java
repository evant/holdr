package me.tatarka.holdr.compile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
            layouts.put(layoutName, new Layout(layoutName, layoutFile, new ArrayList<Ref>(refs)));
        } else {
            layout.merge(refs);
        }
    }

    @Override
    public Iterator<Layout> iterator() {
        return layouts.values().iterator();
    }

    public static class Layout {
        public final String name;
        public final File file;
        public final List<Ref> refs;

        private Layout(String name, File file, List<Ref> refs) {
            this.name = name;
            this.file = file;
            this.refs = refs;
        }

        private void merge(Collection<Ref> newRefs) {
            for (Ref newRef : newRefs) {
                int index = this.refs.indexOf(newRef);
                if (index >= 0) {
                    Ref oldRef = this.refs.get(index);
                    this.refs.set(index, Ref.merge(name, oldRef, newRef));
                } else {
                    this.refs.add(newRef);
                }
            }
        }
        
        public boolean isEmpty() {
            return refs.isEmpty();
        }
    }
}
