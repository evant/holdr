package me.tatarka.holdr.compile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by evan on 8/23/14.
 */
public class Layouts implements Iterable<Layout> {
    private Map<String, Layout.Builder> layoutBuilders = new HashMap<String, Layout.Builder>();

    public void add(Layout.Builder layoutBuilder) {
        Layout.Builder oldLayoutBuilder = layoutBuilders.get(layoutBuilder.name);
        if (oldLayoutBuilder == null) {
            layoutBuilders.put(layoutBuilder.name, layoutBuilder);
        } else {
            oldLayoutBuilder.merge(layoutBuilder);
        }
    }

    @Override
    public Iterator<Layout> iterator() {
        return new Iterator<Layout>() {
            private Iterator<Layout.Builder> builderIterator = layoutBuilders.values().iterator();

            @Override
            public boolean hasNext() {
                return builderIterator.hasNext();
            }

            @Override
            public Layout next() {
                return builderIterator.next().build();
            }

            @Override
            public void remove() {
                builderIterator.remove();
            }
        };
    }
}
