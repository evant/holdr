package me.tatarka.holdr.model;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * The intellij plugin no longer uses this to build the holdr classes and it will be removed in a future version.
 */
@Deprecated
public interface HoldrCompiler {
    public void compile(File outputDirectory, Collection<File> resDirs) throws IOException;

    public void compileIncremental(File outputDirectory, Collection<File> changedFiles, Collection<File> removedFiles) throws IOException;

    public HoldrConfig getConfig();
}