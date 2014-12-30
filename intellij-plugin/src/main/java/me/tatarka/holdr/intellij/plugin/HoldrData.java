package me.tatarka.holdr.intellij.plugin;

import me.tatarka.holdr.model.HoldrCompiler;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by evan on 9/28/14.
 */
public class HoldrData implements Serializable {
    @NotNull private final String myModuleName;
    @NotNull private final HoldrCompiler myCompiler;

    public HoldrData(@NotNull String moduleName, @NotNull HoldrCompiler compiler) {
        this.myModuleName = moduleName;
        this.myCompiler = compiler;
    }

    public String getModuleName() {
        return myModuleName;
    }

    public HoldrCompiler getCompiler() {
        return myCompiler;
    }
}
