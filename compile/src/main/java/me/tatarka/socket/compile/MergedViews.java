package me.tatarka.socket.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MergedViews implements Iterable<Set<View>> {
    private Map<String, Set<View>> layouts = new HashMap<String, Set<View>>();

    public void add(String name, Collection<View> views) {
        Set<View> currentViews = layouts.get(name);
        if (currentViews == null) {
            layouts.put(name, new LinkedHashSet<View>(views));
        } else {
            currentViews.addAll(views);
        }
    }

    private static List<View> merge(List<View> currentViews, List<View> newViews) {
        List<View> mergedViews = new ArrayList<View>();

        for (View currentView : currentViews) {
            for (View newView : newViews) {
                if (!currentView.id.equals(newView.id) || !(currentView.isAndroidId == newView.isAndroidId)) {
                    mergedViews.add(newView);
                }
            }
        }

        return mergedViews;
    }

    @Override
    public Iterator<Set<View>> iterator() {
        return layouts.values().iterator();
    }
}
