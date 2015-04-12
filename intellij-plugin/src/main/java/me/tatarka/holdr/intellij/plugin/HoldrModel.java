package me.tatarka.holdr.intellij.plugin;

import com.android.tools.idea.AndroidPsiUtils;
import com.google.common.base.CaseFormat;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import me.tatarka.holdr.model.HoldrConfig;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by evan on 9/27/14.
 */
public class HoldrModel {
    private static final Logger LOGGER = Logger.getInstance(HoldrModel.class);
    public static final Key<HoldrModel> HOLDR_MODEL_KEY = Key.create("HoldrModelKey");

    @Nullable
    public static synchronized HoldrModel getInstance(@Nullable Module module) {
        if (module == null) {
            return null;
        }

        HoldrModel model = module.getUserData(HOLDR_MODEL_KEY);
        if (model != null) {
            return model;
        }

        AndroidFacet androidFacet = AndroidFacet.getInstance(module);
        if (androidFacet == null) {
            return null;
        }
        HoldrConfig config = HoldrConfigPersister.getConfig(module);
        if (config == null) {
            return null;
        }

        model = new HoldrModel(androidFacet, config);
        module.putUserData(HOLDR_MODEL_KEY, model);
        return model;
    }
    
    @Nullable
    public static synchronized HoldrModel getInstance(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        Module module = AndroidPsiUtils.getModuleSafely(element);
        return getInstance(module);
    }

    public static synchronized void create(@Nullable Module module, @NotNull HoldrConfig config) {
        if (module == null) {
            return;
        }
        HoldrConfigPersister.setConfig(module, config);
    }

    public static synchronized void delete(@Nullable Module module) {
        if (module == null) {
            return;
        }
        module.putUserData(HOLDR_MODEL_KEY, null);
        HoldrConfigPersister.deleteConfig(module);
    }

    private AndroidFacet myAndroidFacet;
    private HoldrConfig myConfig;

    private HoldrModel(AndroidFacet androidFacet, HoldrConfig config) {
        myConfig = config;
        myAndroidFacet = androidFacet;
    }

    public void compile(AndroidFacet androidFacet) {
//        GradleInvoker invoker = GradleInvoker.getInstance(androidFacet.getModule().getProject());
//
//        IdeaAndroidProject ideaAndroidProject = androidFacet.getIdeaAndroidProject();
//        String variantName;
//        if (ideaAndroidProject != null) {
//            variantName = StringUtil.capitalize(androidFacet.getIdeaAndroidProject().getSelectedVariant().getName());
//        } else {
//            variantName = "Debug";
//        }
//
//        String taskName = "generate" + variantName + "Holdr";
//        invoker.executeTasks(Collections.singletonList(taskName));
    }

    public boolean isHoldrClass(@NotNull PsiClass psiClass) {
        String holdrPackage = getConfig().getHoldrPackage();
        String className = psiClass.getQualifiedName();
        return className != null && className.startsWith(holdrPackage + ".Holdr_");
    }

    public String getLayoutName(@NotNull PsiClass psiClass) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, psiClass.getName().replace("Holdr_", ""));
    }

    public String getFieldIdName(@NotNull String fieldName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }

    @Nullable
    public String getHoldrClassName(@NotNull String layoutName) {
        String holdrPackage = getConfig().getHoldrPackage();
        return holdrPackage + "." + getHoldrShortClassName(layoutName);
    }

    public String getHoldrShortClassName(@NotNull String layoutName) {
        return "Holdr_" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, layoutName);
    }

    @NotNull
    public HoldrConfig getConfig() {
        if (myConfig == null) {
            myConfig = new DefaultHoldrConfig(myAndroidFacet);
        }
        return myConfig;
    }

    public void setConfig(HoldrConfig config) {
        myConfig = config;
    }
    
    public AndroidFacet getAndroidFacet() {
        return myAndroidFacet;
    }

    private static class DefaultHoldrConfig implements HoldrConfig {
        @Nullable
        private AndroidFacet myAndroidFacet;

        public DefaultHoldrConfig(@Nullable AndroidFacet androidFacet) {
            myAndroidFacet = androidFacet;
        }

        @Override
        public String getManifestPackage() {
            if (myAndroidFacet == null) {
                return null;
            }
            Manifest manifest = myAndroidFacet.getManifest();
            if (manifest == null) {
                return null;
            }
            return manifest.getPackage().toString();
        }

        @Override
        public String getHoldrPackage() {
            if (myAndroidFacet == null) {
                return null;
            }
            return getManifestPackage() + ".holdr";
        }

        @Override
        public boolean getDefaultInclude() {
            return true;
        }
    }
}
