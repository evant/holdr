package me.tatarka.holdr.intellij.plugin;

import com.android.resources.ResourceFolderType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.util.AndroidCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: evantatarka
 * Date: 10/6/14
 * Time: 8:48 AM
 */
public class HoldrAndroidUtils {
    public static boolean isLayoutFile(@Nullable VirtualFile file) {
        if (file == null) {
            return false;
        }

        final FileType fileType = file.getFileType();

        if (fileType == StdFileTypes.XML) {
            final VirtualFile parent = file.getParent();
            return isLayoutDir(parent);
        }
        return false;
    }

    public static boolean isLayoutDir(@Nullable VirtualFile dir) {
        if (dir != null && dir.isDirectory()) {
            final String resType = AndroidCommonUtils.getResourceTypeByDirName(dir.getName());
            return ResourceFolderType.LAYOUT.getName().equals(resType);
        }
        return false;
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
