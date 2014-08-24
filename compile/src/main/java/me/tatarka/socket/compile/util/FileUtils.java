package me.tatarka.socket.compile.util;

import java.io.File;

public class FileUtils {
    public static File inputToOutput(File inputDir, File outputDir, File file) {
        return new File(outputDir, inputDir.toURI().relativize(file.toURI()).getPath());
    }

    public static File changeName(File file, String newName) {
        return new File(file.getParentFile(), newName);
    }

    public static String stripExtension(String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }
}
