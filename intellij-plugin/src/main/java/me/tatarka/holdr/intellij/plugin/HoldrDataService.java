package me.tatarka.holdr.intellij.plugin;

import com.android.tools.idea.gradle.AndroidProjectKeys;
import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataService;
import com.intellij.openapi.externalSystem.util.DisposeAwareProjectChange;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import me.tatarka.holdr.model.HoldrConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * Created by evan on 9/28/14.
 */
public class HoldrDataService implements ProjectDataService<HoldrData, HoldrData> {
    public static final Key<HoldrData> HOLDR_CONFIG_KEY = Key.create(HoldrData.class, AndroidProjectKeys.IDE_ANDROID_PROJECT.getProcessingWeight() + 9);
    private static final Logger LOGGER = Logger.getInstance(HoldrDataService.class);

    @NotNull
    @Override
    public Key<HoldrData> getTargetDataKey() {
        return HOLDR_CONFIG_KEY;
    }

    @Override
    public void importData(@NotNull final Collection<DataNode<HoldrData>> toImport, @NotNull final Project project, boolean synchronous) {
        if (!toImport.isEmpty()) {
            ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
                @Override
                public void execute() {
                    Map<String, HoldrConfig> holdrConfigMap = indexByModuleName(toImport);
                    ModuleManager moduleManager = ModuleManager.getInstance(project);

                    for (Module module : moduleManager.getModules()) {
                        HoldrConfig holdrConfig = holdrConfigMap.get(module.getName());
                        if (holdrConfig != null) {
                            HoldrModel.create(module, holdrConfig);
                        } else {
                            HoldrModel.delete(module);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void removeData(@NotNull final Collection<? extends HoldrData> toRemove, @NotNull final Project project, boolean synchronous) {
        if (!toRemove.isEmpty()) {
            ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
                @Override
                public void execute() {
                    Map<String, HoldrConfig> holdrConfigMap = indexRemoveByModuleName(toRemove);
                    ModuleManager moduleManager = ModuleManager.getInstance(project);

                    for (Module module : moduleManager.getModules()) {
                        HoldrModel.delete(module);
                    }
                }
            });
        }
    }

    @NotNull
    private static Map<String, HoldrConfig> indexByModuleName(@NotNull Collection<DataNode<HoldrData>> dataNodes) {
        Map<String, HoldrConfig> index = Maps.newHashMap();
        for (DataNode<HoldrData> d : dataNodes) {
            HoldrData data = d.getData();
            index.put(data.getModuleName(), data.getConfig());
        }
        return index;
    }

    @NotNull
    private static Map<String, HoldrConfig> indexRemoveByModuleName(@NotNull Collection<? extends HoldrData> dataNodes) {
        Map<String, HoldrConfig> index = Maps.newHashMap();
        for (HoldrData data : dataNodes) {
            index.put(data.getModuleName(), data.getConfig());
        }
        return index;
    }
}
