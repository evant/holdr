package me.tatarka.holdr.intellij.plugin;

import com.android.builder.model.Variant;
import com.android.tools.idea.gradle.IdeaAndroidProject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import me.tatarka.holdr.compile.HoldrCompiler;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.tatarka.holdr.intellij.plugin.HoldrUtils.getPackageName;

/**
 * Created by evan on 9/27/14.
 */
public class HoldrModel {
    private static final Logger LOGGER = Logger.getInstance(HoldrModel.class);
    public static final Key<HoldrModel> HOLDR_MODEL_KEY = Key.create("HoldrModelKey");

    public static synchronized HoldrModel getInstance(AndroidFacet androidFacet) {
        if (androidFacet == null) {
            return null;
        }

        Module module = androidFacet.getModule();

        HoldrModel model = module.getUserData(HOLDR_MODEL_KEY);

        if (model == null) {
            model = new HoldrModel(androidFacet);
            module.putUserData(HOLDR_MODEL_KEY, model);
        }

        return model;
    }

    private final AndroidFacet myAndroidFacet;
    private HoldrCompiler myCompiler;

    protected HoldrModel(AndroidFacet androidFacet) {
        myAndroidFacet = androidFacet;
        // TODO: get arguments from gradle plugin
        myCompiler = new HoldrCompiler(getPackageName(androidFacet), true);
    }

    public void update(@NotNull Collection<VirtualFile> layoutFiles) {
        List<File> updateFiles = new ArrayList<File>();
        for (VirtualFile file : layoutFiles) {
            updateFiles.add(new File(file.getPath()));
        }
        try {
            myCompiler.compileIncremental(updateFiles, Collections.<File>emptyList(), getOutputDir());
        } catch (IOException e) {
            LOGGER.warn("Error generating Holdr classes", e);
        }
    }

    public void delete(@NotNull Collection<VirtualFile> layoutFiles) {
        List<File> removedFiles = new ArrayList<File>();
        for (VirtualFile file : layoutFiles) {
            removedFiles.add(new File(file.getPath()));
        }
        try {
            myCompiler.compileIncremental(Collections.<File>emptyList(), removedFiles, getOutputDir());
        } catch (IOException e) {
            LOGGER.warn("Error generating Holdr classes", e);
        }
    }

    @Nullable
    public File getOutputDir() {
        IdeaAndroidProject androidProject = myAndroidFacet.getIdeaAndroidProject();
        if (androidProject == null) return null;
        Variant selectedVariant = androidProject.getSelectedVariant();
        return new File(androidProject.getDelegate().getBuildFolder(), "generated/source/holdr/" + selectedVariant.getName());
    }
}
