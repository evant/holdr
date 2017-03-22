package me.tatarka.holdr2

import doesNotExist
import exists
import me.tatarka.assertk.assert
import me.tatarka.assertk.assertAll
import me.tatarka.assertk.assertions.contains
import me.tatarka.assertk.assertions.doesNotContain
import org.gradle.api.Action
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class HoldrTaskTest {

    @Rule
    @JvmField
    val tempDirRule = TemporaryFolder()

    lateinit var task: HoldrTask

    @Before
    fun setup() {
        val project = ProjectBuilder().build()
        task = project.tasks.create("test", HoldrTask::class.java)
    }

    @Test
    fun `generates layout class`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<layout><TextView/></layout>")

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput())

        val output = File(outputDir, "packageName/holdr/layout.java")
        assert(output).exists()
    }

    @Test
    fun `generates layout mapping`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<layout><TextView/></layout>")

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput())

        val output = File(outputDir, "me/tatarka/holdr2/internal/LayoutMapping.java")
        assert(output).exists()
    }

    @Test
    fun `generates added layout class`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<layout><TextView/></layout>")

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput(outOfDate = listOf(added(layout))))

        val output = File(outputDir, "packageName/holdr/layout.java")
        assert(output).exists()
    }

    @Test
    fun `generates modified layout class`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<layout><TextView/></layout>")

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput(outOfDate = listOf(modified(layout))))

        val output = File(outputDir, "packageName/holdr/layout.java")
        assert(output).exists()
    }

    @Test
    fun `deletes removed layout class`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        val output = File(outputDir, "packageName/holdr/layout.java")
        output.parentFile.mkdirs()
        output.writeText("package packageName; public class layout {}")

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput(removed = listOf(removed(layout))))

        assert(output).doesNotExist()
    }

    @Test
    fun `generates layout class from multiple layout variants`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        layout.writeText("<layout><TextView/></layout>")
        val layoutLand = tempDirRule.newFilePath("res/layout-land/layout.xml")
        layoutLand.writeText("<layout><TextView/></layout>")
        val outputDir = tempDirRule.newFolder()

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput())

        val output = File(outputDir, "packageName/holdr/layout.java")
        val outputText = output.readText()
        assertAll {
            assert("output", output).exists()
            assert("text", outputText) {
                it.contains("\"layout/layout\"")
                it.contains("\"layout-land/layout\"")
            }
        }
    }

    @Test
    fun `generates layout class from multiple layout variants where 1 is added`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        layout.writeText("<layout><TextView/></layout>")
        val outputDir = tempDirRule.newFolder()

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput())

        val layoutLand = tempDirRule.newFilePath("res/layout-land/layout.xml")
        layoutLand.writeText("<layout><TextView/></layout>")

        task.process(incrementalTaskInput(outOfDate = listOf(added(layoutLand))))

        val output = File(outputDir, "packageName/holdr/layout.java")
        val outputText = output.readText()
        assertAll {
            assert("output", output).exists()
            assert("text", outputText) {
                it.contains("\"layout/layout\"")
                it.contains("\"layout-land/layout\"")
            }
        }
    }

    @Test
    fun `generates layout class from multiple layout variants where 1 is modified`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        layout.writeText("<layout><TextView/></layout>")
        val outputDir = tempDirRule.newFolder()

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput())

        val layoutLand = tempDirRule.newFilePath("res/layout-land/layout.xml")
        layoutLand.writeText("<layout><TextView/></layout>")

        task.process(incrementalTaskInput(outOfDate = listOf(modified(layoutLand))))

        val output = File(outputDir, "packageName/holdr/layout.java")
        val outputText = output.readText()
        assertAll {
            assert("output", output).exists()
            assert("text", outputText) {
                it.contains("\"layout/layout\"")
                it.contains("\"layout-land/layout\"")
            }
        }
    }

    @Test
    fun `generates layout class from multiple layout variants where 1 is deleted`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        layout.writeText("<layout><TextView/></layout>")
        val layoutLand = tempDirRule.newFilePath("res/layout-land/layout.xml")
        layoutLand.writeText("<layout><TextView/></layout>")
        val outputDir = tempDirRule.newFolder()

        val project = ProjectBuilder().build()
        val task = project.tasks.create("test", HoldrTask::class.java)
        task.packageName = "packageName"
        task.resDir = resDir
        task.outputDir = outputDir

        task.process(incrementalTaskInput())

        task.process(incrementalTaskInput(removed = listOf(removed(layoutLand))))

        val output = File(outputDir, "packageName/holdr/layout.java")
        val outputText = output.readText()
        assertAll {
            assert("output", output).exists()
            assert("text", outputText) {
                it.contains("\"layout/layout\"")
                it.doesNotContain("\"layout-land/layout\"")
            }
        }
    }
}

// Helpers for incremental build info

fun incrementalTaskInput(): IncrementalTaskInputs {
    return object : IncrementalTaskInputs {
        override fun isIncremental(): Boolean = false

        override fun outOfDate(f: Action<in InputFileDetails>) {}

        override fun removed(f: Action<in InputFileDetails>) {}
    }
}

fun incrementalTaskInput(outOfDate: List<InputFileDetails> = emptyList(),
                         removed: List<InputFileDetails> = emptyList()): IncrementalTaskInputs {
    return object : IncrementalTaskInputs {
        override fun isIncremental(): Boolean = true

        override fun outOfDate(f: Action<in InputFileDetails>) {
            outOfDate.forEach { f.execute(it) }
        }

        override fun removed(f: Action<in InputFileDetails>) {
            removed.forEach { f.execute(it) }
        }
    }
}

fun added(file: File): InputFileDetails = object : InputFileDetails {
    override fun getFile(): File = file

    override fun isModified(): Boolean = false

    override fun isAdded(): Boolean = true

    override fun isRemoved(): Boolean = false
}

fun modified(file: File): InputFileDetails = object : InputFileDetails {
    override fun getFile(): File = file

    override fun isModified(): Boolean = true

    override fun isAdded(): Boolean = false

    override fun isRemoved(): Boolean = false
}

fun removed(file: File): InputFileDetails = object : InputFileDetails {
    override fun getFile(): File = file

    override fun isModified(): Boolean = false

    override fun isAdded(): Boolean = false

    override fun isRemoved(): Boolean = true
}
