package me.tatarka.holdr.model;

import me.tatarka.holdr.util.FileUtils;

import java.io.File;
import java.util.List;

/**
* Created by evan on 2/14/15.
*/
public class SingleLayout extends Layout {
    private final File path;
    private final LayoutInfo layoutInfo;
    private final List<Ref> refs;
    private final Listeners listeners;

    SingleLayout(File path, String superclass, boolean isRootMerge, List<Ref> refs, Listeners listeners) {
        this.path = path;
        this.layoutInfo = new LayoutInfo(FileUtils.stripExtension(path.getName()), superclass, isRootMerge);
        this.refs = refs;
        this.listeners = listeners;
    }

    @Override
    public LayoutInfo getLayoutInfo() {
        return layoutInfo;
    }

    @Override
    public List<Ref> getRefs() {
        return refs;
    }

    @Override
    public Listeners getListeners() {
        return listeners;
    }

    public File getPath() {
        return path;
    }
}
