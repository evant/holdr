package me.tatarka.holdr.gradle

import me.tatarka.holdr.compile.HoldrCompiler
import me.tatarka.holdr.compile.HoldrConfigImpl
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails

public class HoldrTask extends DefaultTask {
    @Input
    String manifestPackage

    @Input
    String holdrPackage

    @Input
    boolean defaultInclude

    @InputFiles
    FileCollection resDirectories

    @OutputDirectory
    File outputDirectory

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        logging.captureStandardOutput(LogLevel.INFO)

        def compiler = new HoldrCompiler(new HoldrConfigImpl(manifestPackage, holdrPackage, defaultInclude))

        if (inputs.incremental) {
            List<File> changedFiles = []
            List<File> removedFiles = []
            inputs.outOfDate { InputFileDetails change ->
                changedFiles += change.file
            }
            inputs.removed { InputFileDetails change ->
                removedFiles += change.file
            }
            compiler.compileIncremental(outputDirectory, resDirectories.files, changedFiles, removedFiles)
        } else {
            outputDirectory.deleteDir()
            compiler.compile(outputDirectory, resDirectories.files)
        }
    }
}
