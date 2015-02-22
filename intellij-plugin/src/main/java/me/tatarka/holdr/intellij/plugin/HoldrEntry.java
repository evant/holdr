package me.tatarka.holdr.intellij.plugin;

/**
 * Created by evan on 2/13/15.
 */
public class HoldrEntry {
    private String superClass;
    private boolean isRootMerge;

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public boolean isRootMerge() {
        return isRootMerge;
    }

    public void setRootMerge(boolean isRootMerge) {
        this.isRootMerge = isRootMerge;
    }
}
