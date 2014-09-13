package me.tatarka.holdr.compile.model;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.tatarka.holdr.compile.util.Pair;

/**
 * Created by evan on 9/8/14.
 */
public class Listeners implements Iterable<Listener> {
    private final Map<Listener, Collection<View>> listenerMap;

    private Listeners(@NotNull Map<Listener, Collection<View>> listenerMap) {
        this.listenerMap = listenerMap;
    }
    
    public static Builder of() {
        return new Builder();
    }

    public Collection<Listener> forView(View view) {
        Collection<Listener> result = new ArrayList<Listener>();
        for (Map.Entry<Listener, Collection<View>> entry : listenerMap.entrySet()) {
            Listener listener = entry.getKey();
            Collection<View> views = entry.getValue();
            
            if (views.contains(view)) {
                result.add(listener);
            }
        }
        return result;
    }

    @Override
    public Iterator<Listener> iterator() {
        return listenerMap.keySet().iterator();
    }

    @Override
    public int hashCode() {
        return listenerMap.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Listeners that = (Listeners) o;
        return listenerMap.equals(that.listenerMap);
    }

    public static class Builder {
        private Map<Listener, Collection<View>> listenerMap = new LinkedHashMap<Listener, Collection<View>>();
        
        private Builder() {
            
        }

        public Builder add(View view, Collection<Listener.Builder> listenerBuilders) {
            for (Listener.Builder listenerBuilder : listenerBuilders) {
                Listener newListener = listenerBuilder.build(view.fieldName, view.type, view.fieldName);
                Collection<View> views = listenerMap.remove(newListener);
                if (views == null) {
                    Collection<View> newViews = new ArrayList<View>();
                    newViews.add(view);
                    listenerMap.put(newListener, newViews);
                } else {
                    Listener mergedListener = Listener.of(newListener).build("", newListener.viewType, "view");
                    views.add(view);
                    listenerMap.put(mergedListener, views);
                }
            }
            return this;
        }

        public Listeners build() {
            return new Listeners(listenerMap);
        }
    }
}
