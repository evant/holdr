package me.tatarka.holdr.intellij.plugin;

import me.tatarka.holdr.model.HoldrCompiler;
import me.tatarka.holdr.model.HoldrConfig;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * User: evantatarka
 * Date: 10/3/14
 * Time: 11:01 AM
 */
@Deprecated
public class LegacyHoldrCompiler implements HoldrCompiler {
    private LegacyHoldrConfig myConfig;
    private me.tatarka.holdr.compile.HoldrCompiler myCompiler;

    public LegacyHoldrCompiler(me.tatarka.holdr.compile.model.HoldrConfig config) {
        myConfig = new LegacyHoldrConfig(config);
        myCompiler = new me.tatarka.holdr.compile.HoldrCompiler(config);
    }

    @Override
    public void compile(File outputDirectory, Collection<File> resDirs) throws IOException {
        myCompiler.compile(resDirs, outputDirectory);
    }

    @Override
    public void compileIncremental(File outputDirectory, Collection<File> changedFiles, Collection<File> removedFiles) throws IOException {
        myCompiler.compileIncremental(changedFiles, removedFiles, outputDirectory);
    }

    @Override
    public HoldrConfig getConfig() {
        return myConfig;
    }

    private static class LegacyHoldrConfig implements HoldrConfig {
        private me.tatarka.holdr.compile.model.HoldrConfig myConfig;

        LegacyHoldrConfig(me.tatarka.holdr.compile.model.HoldrConfig config) {
            this.myConfig = config;
        }

        @Override
        public String getManifestPackage() {
            return myConfig.getManifestPackage();
        }

        @Override
        public String getHoldrPackage() {
            return myConfig.getHoldrPackage();
        }

        @Override
        public boolean getDefaultInclude() {
            return myConfig.getDefaultInclude();
        }
    }
}
