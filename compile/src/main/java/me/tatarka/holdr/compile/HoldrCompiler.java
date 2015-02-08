package me.tatarka.holdr.compile;

import me.tatarka.holdr.compile.util.FileUtils;
import me.tatarka.holdr.model.HoldrConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HoldrCompiler {
    private final HoldrConfig config;
    private final HoldrLayoutParser parser;
    private final HoldrGenerator generator;

    public HoldrCompiler(HoldrConfig config) {
        this.config = config;
        parser = new HoldrLayoutParser(config);
        generator = new HoldrGenerator(config);
    }

    public void compile(File outputDirectory, Collection<File> resDirs) throws IOException {
        compileInternal(outputDirectory, resDirs, getAllLayoutFiles(resDirs));
    }

    public void compileIncremental(File outputDirectory, Collection<File> resDirs, Collection<File> changeFiles, Collection<File> removedFiles) throws IOException {
        compileInternal(outputDirectory, resDirs, getChangedLayoutFiles(outputDirectory, changeFiles, removedFiles));
    }

    private void compileInternal(File outputDirectory, Collection<File> resDirs, List<File> layoutFiles) throws IOException {
        System.out.println("Holdr: processing " + layoutFiles.size() + " layout files");
        if (layoutFiles.isEmpty()) return;
        
        Layouts layouts = new Layouts();

        while (!layoutFiles.isEmpty()) {
            for (File layoutFile : layoutFiles) {
                System.out.println("Holdr: processing " + layoutFile.getPath());
                FileReader reader = null;

                String layoutName = FileUtils.stripExtension(layoutFile.getName());

                try {
                    reader = new FileReader(layoutFile);
                    layouts.add(parser.parse(layoutName, reader));
                } finally {
                    if (reader != null) reader.close();
                }
            }
            
            layoutFiles = getExtraIncludeLayoutFiles(resDirs, layouts);
        }
        
        

        for (String layoutName : layouts.getNames()) {
            File outputFile = outputFile(outputDirectory, layoutName);
            Layout layout = layouts.get(layoutName);
            
            if (!layout.isEmpty()) {
                outputFile.getParentFile().mkdirs();

                Writer writer = null;
                try {
                    writer = new FileWriter(outputFile);
                    generator.generate(layout, layouts.asIncludeResolver(), writer);
                    System.out.println("Holdr: created " + outputFile);
                } finally {
                    if (writer != null) writer.close();
                }
            } else if (outputFile.exists()) {
                outputFile.delete();
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

    private List<File> getChangedLayoutFiles(File outputDirectory, Collection<File> changedFiles, Collection<File> removedFiles) {
        List<File> changedLayoutFiles = new ArrayList<File>();
        List<File> layoutFiles = new ArrayList<File>();

        for (File changedFile : changedFiles) {
            if (isLayoutDir(changedFile.getParentFile().getName())) {
                changedLayoutFiles.add(changedFile);
                layoutFiles.add(changedFile);
            }
        }

        for (File removedFile : removedFiles) {
            if (isLayoutDir(removedFile.getParentFile().getName())) {
                changedLayoutFiles.add(removedFile);

                File outputFile = outputFile(outputDirectory, removedFile);
                if (outputFile.exists()) {
                    outputFile.delete();
                }
            }
        }

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
    
    private List<File> getExtraIncludeLayoutFiles(Collection<File> resDirs, Layouts layouts) {
        List<String> includes = layouts.getExtraIncludes();
        List<File> result = new ArrayList<File>();

        for (File inputDir : resDirs) {
            File[] layoutDirs = inputDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("layout");
                }
            });
            if (layoutDirs == null) continue;
            for (File layoutDir : layoutDirs) {
                File[] layoutFiles = layoutDir.listFiles();
                if (layoutFiles != null) {
                    for (File layoutFile : layoutFiles) {
                        if (includes.contains(FileUtils.stripExtension(layoutFile.getName()))) {
                            result.add(layoutFile);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isLayoutDir(String dirName) {
        return dirName.startsWith("layout");
    }

    private File outputFile(File outputDir, String layoutName) {
        String className = generator.getClassName(layoutName);
        return new File(packageToFile(outputDir, config.getHoldrPackage()), className + ".java");
    }

    private File outputFile(File outputDir, File layoutFile) {
        return outputFile(outputDir, FileUtils.stripExtension(layoutFile.getName()));
    }

    private static File packageToFile(File baseDir, String holdrPackage) {
        return new File(baseDir, holdrPackage.replace('.', File.separatorChar));
    }
}
