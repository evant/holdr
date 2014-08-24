package me.tatarka.socket.gradle

import me.tatarka.socket.compile.SocketCompiler
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails

public class SocketTask extends DefaultTask {
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

        def compiler = new SocketCompiler(packageName, defaultInclude)

        if (inputs.incremental) {
            List<File> changedFiles = []
            inputs.outOfDate { InputFileDetails changes ->
                changedFiles += changes.file
            }

            compiler.compileIncremental(changedFiles, outputDirectory)
            
            inputs.removed { InputFileDetails change ->
                File outputFile = compiler.outputFile(outputDirectory, change.file);
                if (outputFile.exists()) outputFile.delete()
            }
        } else {
            compiler.compile(resDirectories.files, outputDirectory)
        }
    }
}
