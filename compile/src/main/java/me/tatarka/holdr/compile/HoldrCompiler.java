package me.tatarka.holdr.compile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import me.tatarka.holdr.compile.util.FileUtils;

public class HoldrCompiler {
    public static final String PACKAGE = "holdr";
    
    private final String packageName;
    private final HoldrViewParser parser;
    private final HoldrGenerator generator;

    public HoldrCompiler(String packageName, boolean defaultInclude) {
        this.packageName = packageName;

        parser = new HoldrViewParser(defaultInclude);
        generator = new HoldrGenerator(packageName);
    }

    public void compile(Collection<File> resDirs, File outputDir) throws IOException {
        compile(outputDir, getAllLayoutFiles(resDirs));
    }
    
    public void compileIncremental(Collection<File> changeFiles, Collection<File> removedFiles, File outputDir) throws IOException {
        compile(outputDir, getChangedLayoutFiles(changeFiles, removedFiles));
    }

    private void compile(File outputDir, List<File> layoutFiles) throws IOException {
        System.out.println("Holdr: processing " + layoutFiles.size() + " layout files");
        if (layoutFiles.isEmpty()) return;

        Layouts layouts = new Layouts();

        for (File layoutFile : layoutFiles) {
            System.out.println("Holdr: processing " + layoutFile.getPath());
            FileReader reader = null;
            try {
                reader = new FileReader(layoutFile);
                List<Ref> refs = parser.parse(reader);
                layouts.add(layoutFile, refs);
            } finally {
                if (reader != null) reader.close();
            }
        }

        for (Layouts.Layout layout : layouts) {
            if (!layout.isEmpty()) {
                File outputFile = outputFile(outputDir, layout.file);
                outputFile.getParentFile().mkdirs();

                Writer writer = null;
                try {
                    writer = new FileWriter(outputFile);
                    generator.generate(layout.name, layout.refs, writer);
                    System.out.println("Holdr: created " + outputFile);
                } finally {
                    if (writer != null) writer.close();
                }
            }
        }
    }

    private static List<File> getAllLayoutFiles(Collection<File> inputDirs) {
        List<File> layoutFiles = new ArrayList<File>();
        for (File inputDir : inputDirs) {
            File[] layoutDirs = inputDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("layout");
                }
            });
            if (layoutDirs == null) continue;
            for (File layoutDir : layoutDirs) {
                File[] layouts = layoutDir.listFiles();
                if (layouts != null) {
                    layoutFiles.addAll(Arrays.asList(layouts));
                }
            }
        }
        return layoutFiles;
    }
    
    private static List<File> getChangedLayoutFiles(Collection<File> changedLayoutFiles, Collection<File> removedFiles) {
        List<File> layoutFiles = new ArrayList<File>(changedLayoutFiles);

        changedLayoutFiles.addAll(removedFiles);
        for (File file : changedLayoutFiles) {
            File inputDir = file.getParentFile().getParentFile();

            File[] layoutDirs = inputDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("layout");
                }
            });

            if (layoutDirs == null) continue;

            for (File layoutDir : layoutDirs) {
                // Skip dir that the file is in.
                if (layoutDir.getName().equals(file.getParentFile().getName())) {
                    continue;
                }

                File otherFile = new File(layoutDir, file.getName());
                if (otherFile.exists()) {
                    layoutFiles.add(otherFile);
                }
            }
        }
        
        return layoutFiles;
    }

    public File outputFile(File outputDir, File layoutFile) {
        String className = generator.getClassName(FileUtils.stripExtension(layoutFile.getName()));
        return new File(packageToFile(outputDir, packageName), className + ".java");
    }

    private static File packageToFile(File baseDir, String packageName) {
        return new File(baseDir, (packageName + "." + PACKAGE).replaceAll("\\.", File.separator));
    }
}
