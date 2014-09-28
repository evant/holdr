package me.tatarka.holdr.gradle

import me.tatarka.holdr.compile.HoldrCompiler
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
    String packageName

    @Input
    boolean defaultInclude

    @InputFiles
    FileCollection resDirectories

    @OutputDirectory
    File outputDirectory

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        logging.captureStandardOutput(LogLevel.INFO)

        def compiler = new HoldrCompiler(packageName, defaultInclude)

        if (inputs.incremental) {
            List<File> changedFiles = []
            List<File> removedFiles = []
            inputs.outOfDate { InputFileDetails changes ->
                changedFiles += changes.file
            }
            
            inputs.removed { InputFileDetails change ->
                removedFiles += change.file
            }

            compiler.compileIncremental(changedFiles, removedFiles, outputDirectory)
        } else {
            outputDirectory.deleteDir()
            compiler.compile(resDirectories.files, outputDirectory)
        }
    }
}
