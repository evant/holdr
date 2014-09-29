package me.tatarka.holdr.intellij.plugin;

import me.tatarka.holdr.compile.model.HoldrConfig;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by evan on 9/28/14.
 */
public class HoldrData implements Serializable {
    @NotNull private final String myModuleName;
    @NotNull private final HoldrConfig myConfig;

    public HoldrData(@NotNull String moduleName, @NotNull HoldrConfig config) {
        this.myModuleName = moduleName;
        this.myConfig = config;
    }

    public String getModuleName() {
        return myModuleName;
    }

    public HoldrConfig getConfig() {
        return myConfig;
    }
}
