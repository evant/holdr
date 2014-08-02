package me.tatarka.socket.gradle

import me.tatarka.socket.compile.SocketCompiler
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails;

public class SocketTask extends DefaultTask {
    @Input
    String packageName

    @Input
    File resDir

    @Input
    File outputDir

    public SocketTask() {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        logging.captureStandardOutput(LogLevel.INFO)

        def compiler = new SocketCompiler(packageName)

        if (inputs.incremental) {
            inputs.removed { InputFileDetails change ->
                File outputFile = compiler.outputFile(outputDir, change.file);
                if (outputFile.exists()) outputFile.delete()
            }

            List<File> changedFiles = []
            inputs.outOfDate { InputFileDetails changes ->
                changedFiles += changes.file
            }

            compiler.compile(resDir, outputDir, changedFiles)
        } else {
            compiler.compile(resDir, outputDir)
        }
    }
}
