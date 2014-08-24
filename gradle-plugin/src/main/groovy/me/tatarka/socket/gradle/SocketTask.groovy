package me.tatarka.socket.gradle

import me.tatarka.socket.compile.SocketCompiler
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails;

public class SocketTask extends DefaultTask {
    @Input
    String packageName

    @InputDirectory
    File resDir

    @OutputDirectory
    File outputDir

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        logging.captureStandardOutput(LogLevel.INFO)

        def compiler = new SocketCompiler(packageName)

        if (inputs.incremental) {
            List<File> changedFiles = []
            inputs.outOfDate { InputFileDetails changes ->
                changedFiles += changes.file
            }

            compiler.compile(resDir, outputDir, changedFiles)
            
            inputs.removed { InputFileDetails change ->
                File outputFile = compiler.outputFile(outputDir, change.file);
                if (outputFile.exists()) outputFile.delete()
            }
        } else {
            compiler.compile(resDir, outputDir)
        }
    }
}
