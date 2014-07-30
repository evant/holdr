package me.tatarka.socket.compile;

import me.tatarka.socket.compile.util.FileUtils;
import me.tatarka.socket.compile.util.FormatUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SocketCompiler {
    private final String packageName;
    private final SocketViewParser parser;
    private final SocketGenerator generator;

    public SocketCompiler(String packageName) {
        this.packageName = packageName;

        parser = new SocketViewParser();
        generator = new SocketGenerator(packageName);
    }

    public void compile(File inputDir, File outputDir) throws IOException {
        compile(inputDir, outputDir, null);
    }

    public void compile(File inputDir, File outputDir, List<File> layoutFiles) throws IOException {
        System.out.println("Socket: processing layouts in: " + inputDir);

        if (layoutFiles == null) {
            File[] allFiles = inputDir.listFiles();
            if (allFiles == null) return;
            layoutFiles = Arrays.asList(allFiles);
        }

        System.out.println("Socket: found " + layoutFiles.size() + " layout files");
        if (layoutFiles.isEmpty()) return;

        SocketViewParser parser = new SocketViewParser();
        SocketGenerator generator = new SocketGenerator(packageName);

        for (File layoutFile : layoutFiles) {

            FileReader reader = null;
            FileWriter writer = null;

            try {
                reader = new FileReader(layoutFile);
                List<View> views = parser.parse(reader);

                if (!views.isEmpty()) {
                    String layoutName = stripExtension(layoutFile.getName());

                    File outputFile = inputToOutput(inputDir, outputDir, layoutFile);
                    outputFile.getParentFile().mkdirs();

                    writer = new FileWriter(outputFile);
                    generator.generate(layoutName, views, writer);
                    System.out.println("Socket: created " + outputFile);
                }
            } finally {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            }
        }
    }

    public File inputToOutput(File inputDir, File outputDir, File layoutFile) {
        String className = generator.getClassName(stripExtension(layoutFile.getName()));
        return FileUtils.inputToOutput(inputDir, packageToFile(outputDir, packageName), FileUtils.changeName(layoutFile, className + ".java"));
    }

    private static File packageToFile(File baseDir, String packageName) {
        return new File(baseDir, (packageName + ".sockets").replaceAll("\\.", File.separator));
    }

    private static String stripExtension(String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }
}
