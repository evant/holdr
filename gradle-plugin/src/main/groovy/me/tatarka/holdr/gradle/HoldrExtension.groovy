package me.tatarka.holdr.gradle;

/**
 * Created by evan on 8/24/14.
 */
class HoldrExtension {
    boolean defaultInclude = true

    public void defaultInclude(value) {
        defaultInclude = value
    }
}
