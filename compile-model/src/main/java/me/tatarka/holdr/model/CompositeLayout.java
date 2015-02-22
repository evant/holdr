package me.tatarka.holdr.model;

import java.io.File;
import java.util.*;

/**
 * Created by evan on 2/14/15.
 */
public class CompositeLayout extends Layout {
    private Map<File, Layout> layouts = new HashMap<File, Layout>();
    private boolean isDirty = true;
    
    private LayoutInfo layoutInfo;
    private List<Ref> refs;
    private Listeners listeners;
    
    public void put(SingleLayout layout) {
        Layout oldLayout = this.layouts.put(layout.getPath(), layout);
        if (!isDirty) {
            isDirty = oldLayout == null || !oldLayout.equals(layout);
        }
    }
    
    public void remove(SingleLayout layout) {
        Layout removedLayout = this.layouts.remove(layout.getPath());
        if (!isDirty) {
            isDirty = removedLayout != null;
        }
    }

    @Override
    public LayoutInfo getLayoutInfo() {
        ensureMerged();
        return layoutInfo;
    }

    @Override
    public List<Ref> getRefs() {
        ensureMerged();
        return refs;
    }

    @Override
    public Listeners getListeners() {
        ensureMerged();
        return listeners;
    }
    
    private void ensureMerged() {
        if (refs != null && listeners != null && !isDirty) {
            return;
        }
        
        List<LayoutInfo> layoutInfos = new ArrayList<LayoutInfo>(layouts.size());
        List<List<Ref>> allRefs = new ArrayList<List<Ref>>(layouts.size());
        List<Listeners> allListeners = new ArrayList<Listeners>(layouts.size());
        
        for (Layout layout : layouts.values()) {
            layoutInfos.add(layout.getLayoutInfo());
            allRefs.add(layout.getRefs());
            allListeners.add(layout.getListeners());
        }
        
        layoutInfo = LayoutInfo.merge(layoutInfos);
        if (layoutInfo != null) {
            refs = mergeRefs(layoutInfo.getName(), allRefs);
            listeners = mergeListeners(allListeners);
        } else {
            refs = Collections.emptyList();
            listeners = Listeners.of().build();
        }
        
        isDirty = false;
    }

    private static List<Ref> mergeRefs(String name, Collection<List<Ref>> allRefs) {
        if (allRefs.isEmpty()) {
            return Collections.emptyList();
        }

        Iterator<List<Ref>> iterator = allRefs.iterator();
        List<Ref> first = iterator.next();
        List<Ref> result = new ArrayList<Ref>(first);
        
        while (iterator.hasNext()) {
            List<Ref> next = iterator.next();
            mergeRefs(name, result, next);
        }
        
        return result;
    }

    private static void mergeRefs(String name, List<Ref> refs, List<Ref> newRefs) {
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
    
    private static Listeners mergeListeners(Collection<Listeners> allListeners) {
        if (allListeners.isEmpty()) {
            return Listeners.of().build();
        }
        // TODO: this isn't right is it?
        return allListeners.iterator().next();
    }
}
