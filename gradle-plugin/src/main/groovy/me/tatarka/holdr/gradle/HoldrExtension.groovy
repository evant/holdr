package me.tatarka.holdr.gradle

/**
 * Created by evan on 8/24/14.
 */
class HoldrExtension {
    private HoldrPlugin plugin

    boolean defaultInclude = true
    String holdrPackage = null

    public HoldrExtension(HoldrPlugin plugin) {
        this.plugin = plugin
    }

    public void defaultInclude(boolean value) {
        defaultInclude = value
    }

    public void holdrPackage(String name) {
        holdrPackage = name
    }

    public String getHoldrPackage() {
        if (holdrPackage == null) {
            holdrPackage = plugin.manifestPackage + ".holdr"
        }
        return holdrPackage
    }
}
