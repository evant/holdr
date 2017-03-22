package me.tatarka.holdr2

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import okio.Okio
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.*
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import java.io.File
import java.io.IOException
import java.util.*

class HoldrPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withType(AppPlugin::class.java) {
            val extension = project.extensions.getByType(AppExtension::class.java)

            extension.applicationVariants.all { variant ->
                val mergedResDir = variant.mergeResources.outputDir
                val layoutDir = File(variant.mergeResources.outputDir.parentFile.parentFile, "holdr/${variant.name}")
                val holdrTask = project.tasks.create("${variant.name}GenerateHoldr2Sources", HoldrTask::class.java) {
                    it.packageName = variant.generateBuildConfig.appPackageName
                    it.resDir = layoutDir
                    it.outputDir = File(project.buildDir, "generated/source/holdr/${variant.name}")
                }
                holdrTask.onlyIf { layoutDir.exists() }
                holdrTask.dependsOn(variant.mergeResources)
                val postProcessLayouts = PostProcessLayouts().apply {
                    resDir = mergedResDir
                    outputDir = layoutDir
                }
                variant.mergeResources.doLast { postProcessLayouts.process() }

                variant.registerJavaGeneratingTask(holdrTask, holdrTask.outputDir)
            }
        }
    }
}

class PostProcessLayouts {
    lateinit var resDir: File
    lateinit var outputDir: File

    fun process() {
        if (resDir.exists()) {
            val filesToDelete = outputDir.children()
                    .flatMap(File::children)
                    .toMutableList()
            resDir.listFiles()?.forEach { dir ->
                if (dir.name.startsWith("layout")) {
                    dir.listFiles()?.forEach { file ->
                        val tmpFileName = File(dir, file.name + ".tmp")
                        val outFileName = File(outputDir, dir.name + "/" + file.name)
                        var stripResult = StripResult.SKIPPED
                        Okio.buffer(Okio.source(file)).use { source ->
                            val layout = Layout(dir.name, source)
                            stripResult = stripLayout(file.name, layout) {
                                Okio.buffer(Okio.sink(tmpFileName))
                            }
                        }
                        if (stripResult == StripResult.STRIPPED) {
                            println(tmpFileName.readText())
                            println("copy $file -> $outFileName")
                            if (outFileName.exists()) {
                                outFileName.delete()
                            }
                            file.copyTo(outFileName)
                            if (!tmpFileName.renameTo(file)) {
                                throw IOException("failed to rename file: $tmpFileName to $file")
                            }
                        }
                        if (stripResult != StripResult.SKIPPED) {
                            filesToDelete.removeAll {
                                it.parentFile.name == dir.name && it.name == file.name
                            }
                        }
                    }
                }
            }
            for (file in filesToDelete) {
                println("delete $file")
                file.delete()
            }
        }
    }
}

open class HoldrTask : DefaultTask() {

    @Input
    lateinit var packageName: String

    @SkipWhenEmpty
    @InputDirectory
    lateinit var resDir: File

    @OutputDirectory
    lateinit var outputDir: File

    @TaskAction
    fun process(inputs: IncrementalTaskInputs) {
        val packageDir = File(outputDir, packageName.replace('.', File.separatorChar))
        val holdrDir = File(packageDir, "holdr")
        holdrDir.mkdirs()
        val processor = Processor(packageName + ".holdr")

        val layouts = resDir.children()
                .filter { it.name.startsWith("layout") }
                .flatMap(File::children)

        if (inputs.isIncremental) {
            val changed = arrayListOf<File>()
            inputs.outOfDate { change -> changed.add(change.file) }

            // Add all variants for each changed layout
            for (addition in ArrayList(changed)) {
                layouts.filterTo(changed) { it != addition && it.name == addition.name }
            }

            inputs.removed { change ->
                var variantExists = false
                for (layout in layouts) {
                    if (layout != change.file && layout.name == change.file.name) {
                        variantExists = true
                        if (!changed.contains(layout)) {
                            changed.add(layout)
                        }
                    }
                }
                if (!variantExists) {
                    val outFile = getOutFile(processor, holdrDir, change.file.name)
                    outFile.delete()
                }
            }

            genFiles(processor, holdrDir, changed)

        } else {
            project.delete(outputDir.listFiles())
            genFiles(processor, holdrDir, layouts)
        }

        val layoutMapping = LayoutMapping(packageName)
        val layoutMappingDir = File(outputDir, "me/tatarka/holdr2/internal")
        layoutMappingDir.mkdirs()
        Okio.buffer(Okio.sink(File(layoutMappingDir, "LayoutMapping.java"))).use { sink ->
            layoutMapping.process(layouts.map { it.name.removeSuffix(".xml") }.distinct().sorted(), sink)
        }
    }

    private fun genFiles(processor: Processor, packageDir: File, inputs: List<File>) {
        if (inputs.isEmpty()) {
            return
        }
        val layoutMap = hashMapOf<String, ArrayList<File>>()
        for (input in inputs) {
            val name = input.name
            layoutMap.computeIfAbsent(name, { arrayListOf() }).add(input)
        }
        for ((name, layouts) in layoutMap) {
            println("gen files: ${layouts.joinToString(",")}")
            genFile(processor, packageDir, name, layouts)
        }
    }

    private fun genFile(processor: Processor, packageDir: File, layoutFileName: String, layoutFiles: List<File>) {
        val outFile = getOutFile(processor, packageDir, layoutFileName)
        outFile.parentFile.mkdirs()
        val layouts = layoutFiles.map {
            Layout(it.parentFile.name, Okio.buffer(Okio.source(it)))
        }
        Okio.buffer(Okio.sink(outFile)).use { sink ->
            processor.process(layoutFileName, layouts, sink)
        }
        layouts.forEach { it.source.close() }
    }

    private fun getOutFile(processor: Processor, packageDir: File, layoutFileName: String): File {
        val className = processor.simpleClassName(layoutFileName)
        return File(packageDir, className + ".java")
    }
}

private fun File.children(): List<File> = listFiles()?.asList() ?: emptyList<File>()