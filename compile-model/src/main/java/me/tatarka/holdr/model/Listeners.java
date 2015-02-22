package me.tatarka.holdr.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by evan on 9/8/14.
 */
public class Listeners implements Iterable<Listener> {
    private final Map<Listener, Collection<String>> listenerMap;

    private Listeners(Map<Listener, Collection<String>> listenerMap) {
        this.listenerMap = listenerMap;
    }

    public static Builder of() {
        return new Builder();
    }

    public Collection<Listener> forView(View view) {
        Collection<Listener> result = new ArrayList<Listener>();
        for (Map.Entry<Listener, Collection<String>> entry : listenerMap.entrySet()) {
            Listener listener = entry.getKey();
            Collection<String> viewIds = entry.getValue();
            for (String id : viewIds) {
                if (id.equals(view.id)) {
                    result.add(listener);
                    break;
                }
            }
        }
        return result;
    }


    public Collection<String> getViewIds(Listener listener) {
        return listenerMap.get(listener);
    }

    @Override
    public Iterator<Listener> iterator() {
        return listenerMap.keySet().iterator();
    }
    
    public int size() {
        return listenerMap.size();
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
        private Map<Listener, Collection<String>> listenerMap = new LinkedHashMap<Listener, Collection<String>>();

        private Builder() {

        }

        public Builder add(View view, Collection<Listener.Builder> listenerBuilders) {
            for (Listener.Builder listenerBuilder : listenerBuilders) {
                Listener newListener = listenerBuilder.build(view.fieldName, view.type, view.fieldName);
                add(Collections.singletonList(view.id), newListener);
            }
            return this;
        }
        
        public Builder add(Collection<String> newViewIds, Listener newListener) {
            Collection<String> viewIds = listenerMap.remove(newListener);
            if (viewIds == null) {
                listenerMap.put(newListener, new ArrayList<String>(newViewIds));
            } else {
                Listener mergedListener = Listener.of(newListener).build("", newListener.viewType, "view");
                viewIds.addAll(newViewIds);
                listenerMap.put(mergedListener, viewIds);
            }
            return this;
        }

        public Listeners build() {
            return new Listeners(listenerMap);
        }
    }
}
