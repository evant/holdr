package me.tatarka.holdr2

import io.kotlintest.TestBase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class PostProcessLayoutsTest : TestBase() {

    @Rule
    @JvmField
    val tempDirRule = TemporaryFolder()

    lateinit var postProcessLayouts: PostProcessLayouts

    @Before
    fun setup() {
        postProcessLayouts = PostProcessLayouts()
    }

    @Test
    fun `strips layout tags from layout`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<layout><TextView/></layout>")

        postProcessLayouts.resDir = resDir
        postProcessLayouts.outputDir = outputDir

        postProcessLayouts.process()

        assert(!layout.readText().contains("<layout>"))
    }

    @Test
    fun `saves original file in output dir`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<layout><TextView/></layout>")

        postProcessLayouts.resDir = resDir
        postProcessLayouts.outputDir = outputDir

        postProcessLayouts.process()

        val output = File(outputDir, "layout/layout.xml")
        output should exist()
        assert(output.readText().contains("<layout>"))
    }

    @Test
    fun `ignores layouts without layout tags`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<TextView/>")

        postProcessLayouts.resDir = resDir
        postProcessLayouts.outputDir = outputDir

        postProcessLayouts.process()

        val output = File(outputDir, "layout/layout.xml")
        output should notExist()
    }

    @Test
    fun `deletes output if input is missing`() {
        val resDir = tempDirRule.newFolder("res")
        val outputDir = tempDirRule.newFolder()
        val output = File(outputDir, "layout/layout.xml")
        output.parentFile.mkdirs()
        output.writeText("<TextView/>")

        postProcessLayouts.resDir = resDir
        postProcessLayouts.outputDir = outputDir

        postProcessLayouts.process()

        output should notExist()
    }

    @Test
    fun `deletes output if input removes layout tags`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<TextView/>")
        val output = File(outputDir, "layout/layout.xml")
        output.parentFile.mkdirs()
        output.writeText("<TextView/>")

        postProcessLayouts.resDir = resDir
        postProcessLayouts.outputDir = outputDir

        postProcessLayouts.process()

        output should notExist()
    }

    @Test
    fun `skips over already stripped layout`() {
        val resDir = tempDirRule.newFolder("res")
        val layout = tempDirRule.newFilePath("res/layout/layout.xml")
        val outputDir = tempDirRule.newFolder()
        layout.writeText("<TextView xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/layout\"/>")
        val output = File(outputDir, "layout/layout.xml")
        output.parentFile.mkdirs()
        output.writeText("<layout><TextView/></layout>")

        postProcessLayouts.resDir = resDir
        postProcessLayouts.outputDir = outputDir

        postProcessLayouts.process()

        layout should exist()
        output should exist()
    }
}

