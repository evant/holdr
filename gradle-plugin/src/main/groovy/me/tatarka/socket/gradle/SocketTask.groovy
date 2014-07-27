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

        File layoutsDir = new File(resDir, "layout")
        def compiler = new SocketCompiler(packageName)

        if (inputs.incremental) {
            inputs.removed { InputFileDetails change ->
                File outputFile = compiler.inputToOutput(layoutsDir, resDir, change.file)
                if (outputFile.exists()) outputFile.delete()
            }

            List<File> changedFiles = []
            inputs.outOfDate { InputFileDetails changes ->
                changedFiles += changes.file
            }

            compiler.compile(layoutsDir, outputDir, changedFiles)
        } else {
            compiler.compile(layoutsDir, outputDir)
        }
    }
}
