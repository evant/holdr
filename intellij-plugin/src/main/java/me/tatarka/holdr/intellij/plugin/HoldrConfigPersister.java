package me.tatarka.holdr.intellij.plugin;

import com.intellij.openapi.components.*;
import com.intellij.openapi.module.Module;
import com.intellij.util.xmlb.XmlSerializerUtil;
import me.tatarka.holdr.model.HoldrConfig;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by evan on 1/18/15.
 */

@State(name = "HoldrConfigs", storages = {
        @Storage(id = "holdr", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "holdr", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/holdr.xml", scheme = StorageScheme.DIRECTORY_BASED)
})
public class HoldrConfigPersister implements PersistentStateComponent<HoldrConfigPersister> {
    private Map<String, SavedHoldrConfig> myConfigs = new HashMap<String, SavedHoldrConfig>();

    public static HoldrConfig getConfig(Module module) {
        HoldrConfigPersister persister = ServiceManager.getService(module.getProject(), HoldrConfigPersister.class);
        return persister.myConfigs.get(module.getName());
    }

    public static void setConfig(Module module, HoldrConfig config) {
        HoldrConfigPersister persister = ServiceManager.getService(module.getProject(), HoldrConfigPersister.class);
        persister.myConfigs.put(module.getName(), saveableConfig(config));
    }

    public static void deleteConfig(Module module) {
        HoldrConfigPersister persister = ServiceManager.getService(module.getProject(), HoldrConfigPersister.class);
        persister.myConfigs.remove(module.getName());
    }

    @Nullable
    @Override
    public HoldrConfigPersister getState() {
        return this;
    }

    @Override
    public void loadState(HoldrConfigPersister state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public Map<String, SavedHoldrConfig> getConfigs() {
        return myConfigs;
    }

    public void setConfigs(Map<String, SavedHoldrConfig> configs) {
        myConfigs = configs;
    }

    private static SavedHoldrConfig saveableConfig(HoldrConfig config) {
        if (config instanceof SavedHoldrConfig) {
            return (SavedHoldrConfig) config;
        } else {
            return new SavedHoldrConfig(config);
        }
    }

    /**
     * We need a config with setters on it to be able to persist and restore it.
     */
    public static class SavedHoldrConfig implements HoldrConfig {
        private String myManifestPackage;
        private String myHoldrPackage;
        private boolean myDefaultInclude;

        public SavedHoldrConfig() {
        }

        SavedHoldrConfig(HoldrConfig config) {
            myManifestPackage = config.getManifestPackage();
            myHoldrPackage = config.getHoldrPackage();
            myDefaultInclude = config.getDefaultInclude();
        }

        @Override
        public String getManifestPackage() {
            return myManifestPackage;
        }

        public void setManifestPackage(String manifestPackage) {
            myManifestPackage = manifestPackage;
        }

        @Override
        public String getHoldrPackage() {
            return myHoldrPackage;
        }

        public void setHoldrPackage(String holdrPackage) {
            myHoldrPackage = holdrPackage;
        }

        @Override
        public boolean getDefaultInclude() {
            return myDefaultInclude;
        }

        public void setDefaultInclude(boolean defaultInclude) {
            myDefaultInclude = defaultInclude;
        }
    }
}

