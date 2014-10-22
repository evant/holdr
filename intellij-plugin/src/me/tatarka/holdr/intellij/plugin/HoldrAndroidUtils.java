package me.tatarka.holdr.intellij.plugin;

import com.android.builder.model.SourceProvider;
import com.android.resources.ResourceFolderType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * User: evantatarka
 * Date: 10/6/14
 * Time: 8:48 AM
 */
public class HoldrAndroidUtils {
    public static boolean isUserLayoutFile(@NotNull Project project, @Nullable VirtualFile file) {
        if (file == null) {
            return false;
        }

        final FileType fileType = file.getFileType();

        if (fileType == StdFileTypes.XML) {
            final VirtualFile parent = file.getParent();
            return isUserLayoutDir(project, parent);
        }
        return false;
    }

    public static boolean isUserLayoutDir(@NotNull Project project, @Nullable VirtualFile dir) {
        if (dir == null || !dir.isDirectory()) {
            return false;
        }

        Module module = ModuleUtilCore.findModuleForFile(dir, project);
        if (module == null) {
            return false;
        }

        AndroidFacet androidFacet = AndroidFacet.getInstance(module);
        if (androidFacet == null) {
            return false;
        }

        SourceProvider sourceProvider = androidFacet.getVariantSourceProvider();
        if (sourceProvider == null) {
            return false;
        }

        Collection<File> resDirs = sourceProvider.getResDirectories();
        File resDir = new File(dir.getParent().getPath());

        final String resType = AndroidCommonUtils.getResourceTypeByDirName(dir.getName());
        return ResourceFolderType.LAYOUT.getName().equals(resType) && resDirs.contains(resDir);
    }

    public static boolean areIdsEquivalent(@NotNull String fieldId, @NotNull String xmlId) {
        boolean isFieldAndroid = fieldId.startsWith("android.");
        boolean isXmlAndroid = xmlId.startsWith("@android:id/");

        if (isFieldAndroid != isXmlAndroid) {
            return false;
        }

        String fieldName = fieldId.substring(fieldId.lastIndexOf('.') + 1);
        String xmlName = xmlId.substring(xmlId.lastIndexOf('/') + 1);

        return fieldName.equals(xmlName);
    }
}
