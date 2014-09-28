package me.tatarka.holdr.intellij.plugin;

import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;

/**
 * Created by evan on 9/27/14.
 */
public class HoldrUtils {
    public static String getPackageName(@NotNull AndroidFacet facet) {
        final Manifest manifest = facet.getManifest();
        if (manifest == null) {
            return null;
        }

        return manifest.getPackage().getValue();
    }
}
