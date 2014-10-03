package me.tatarka.holdr.model;

import java.io.File;

/**
 * Created by evan on 9/28/14.
 */
public interface HoldrConfig {
    public String getManifestPackage();
    public String getHoldrPackage();
    public boolean getDefaultInclude();
}
