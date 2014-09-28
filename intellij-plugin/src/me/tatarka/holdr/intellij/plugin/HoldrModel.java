package me.tatarka.holdr.intellij.plugin;

import com.android.resources.ResourceFolderType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import me.tatarka.holdr.compile.*;
import me.tatarka.holdr.compile.util.FileUtils;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidCommonUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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
    private HoldrLayoutParser myParser;
    private HoldrGenerator myGenerator;
    private Layouts myLayouts;
    private Map<String, Layout> myLayoutsByClassName = new HashMap<String, Layout>();

    protected HoldrModel(AndroidFacet androidFacet) {
        myAndroidFacet = androidFacet;
        myGenerator = new HoldrGenerator(getPackageName(androidFacet));
        myParser = new HoldrLayoutParser(true);
    }

    public void invalidateLayouts() {
        myLayouts = null;
        myLayoutsByClassName.clear();
    }

    public Layouts getLayouts() {
        ensureLayouts();
        return myLayouts;
    }

    private void ensureLayouts() {
        if (myLayouts == null) {
            myLayouts = new Layouts();
            for (VirtualFile resDirs : myAndroidFacet.getAllResourceDirectories()) {
                for (VirtualFile resDir : resDirs.getChildren()) {
                    if (ResourceFolderType.LAYOUT.getName().equals(AndroidCommonUtils.getResourceTypeByDirName(resDir.getName()))) {
                        for (VirtualFile file : resDir.getChildren()) {
                            try {
                                myLayouts.add(readLayout(myParser, file));
                            } catch (IOException e) {
                                LOGGER.error("Error reading layout file: " + file.getPath(), e);
                            }
                        }
                    }
                }
            }
            myLayoutsByClassName = createLayoutsByClassName(myLayouts);
        }
    }

    public Layout getLayoutForClass(String className) {
        ensureLayouts();
        return myLayoutsByClassName.get(className);
    }

    private Map<String, Layout> createLayoutsByClassName(Layouts layouts) {
        Map<String, Layout> layoutMap = new HashMap<String, Layout>();
        for (Layout layout : layouts) {
            layoutMap.put(myGenerator.getClassName(layout.name), layout);
        }
        return layoutMap;
    }

    private static Layout.Builder readLayout(HoldrLayoutParser parser, VirtualFile file) throws IOException {
        String layoutName = FileUtils.stripExtension(file.getName());
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(file.getInputStream());
            return parser.parse(layoutName, reader);
        } finally {
            if (reader != null) reader.close();
        }
    }

    public String getClassName(String layoutName) {
        return myGenerator.getClassName(layoutName);
    }

    public String getQualifiedClassName(String layoutName) {
        return getHoldrPackage() + "." + getClassName(layoutName);
    }

    public boolean isHoldrClass(PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return false;
        }
        PsiJavaFile javaFile = (PsiJavaFile) file;
        return javaFile.getPackageName().equals(getHoldrPackage());
    }

    public String getHoldrPackage() {
        final String manifestPackage = getPackageName(myAndroidFacet);
        if (manifestPackage == null) {
            return null;
        }

        return manifestPackage + "." + HoldrCompiler.PACKAGE;
    }
}
