package me.tatarka.holdr.compile.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
        private Map<View, Collection<Listener.Builder>> listenerBuilderMap = new LinkedHashMap<View, Collection<Listener.Builder>>();
        
        private Builder() {
            
        }

        public Builder add(View view, Collection<Listener.Builder> listenerBuilders) {
            listenerBuilderMap.put(view, listenerBuilders);
            return this;
        }

        public Listeners build() {
            Map<Listener, Collection<View>> listenerMap = new LinkedHashMap<Listener, Collection<View>>();
            for (Map.Entry<View, Collection<Listener.Builder>> entry : listenerBuilderMap.entrySet()) {
                View view = entry.getKey();
                Collection<Listener.Builder> listenerBuilders = entry.getValue();
                for (Listener.Builder listenerBuilder : listenerBuilders) {
                    listenerMap.put(listenerBuilder.build(view.fieldName, view.type, view.fieldName), Collections.singletonList(view));
                }
            }

            return new Listeners(listenerMap);
        }
    }
}
