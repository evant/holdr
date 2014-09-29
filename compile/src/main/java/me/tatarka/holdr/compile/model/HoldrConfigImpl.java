package me.tatarka.holdr.compile.model;

import java.io.Serializable;

/**
 * Created by evan on 9/28/14.
 */
public final class HoldrConfigImpl implements HoldrConfig, Serializable {
    private final String manifestPackage;
    private final String holdrPackage;
    private final boolean defaultInclude;

    public HoldrConfigImpl(String manifestPackage, String holdrPackage, boolean defaultInclude) {
        this.manifestPackage = manifestPackage;
        this.holdrPackage = holdrPackage;
        this.defaultInclude = defaultInclude;
    }

    @Override
    public String getManifestPackage() {
        return manifestPackage;
    }

    @Override
    public String getHoldrPackage() {
        return holdrPackage;
    }

    @Override
    public boolean getDefaultInclude() {
        return defaultInclude;
    }
}
