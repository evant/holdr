package me.tatarka.holdr.model;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * User: evantatarka
 * Date: 10/2/14
 * Time: 5:11 PM
 */
public interface HoldrCompiler {
    public void compile(File outputDirectory, Collection<File> resDirs) throws IOException;
    public void compileIncremental(File outputDirectory, Collection<File> resDirs, Collection<File> changedFiles, Collection<File> removedFiles) throws IOException;
    public HoldrConfig getConfig();
}
