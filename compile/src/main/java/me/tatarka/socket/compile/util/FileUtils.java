package me.tatarka.socket.compile.util;

import java.io.File;

public class FileUtils {
    public static File inputToOutput(File inputDir, File outputDir, File file) {
        return new File(outputDir, inputDir.toURI().relativize(file.toURI()).getPath());
    }

    public static File changeName(File file, String newName) {
        return new File(file.getParentFile(), newName);
    }
}
