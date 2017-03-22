package me.tatarka.holdr2

import io.kotlintest.TestBase
import okio.Okio
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(org.junit.runners.JUnit4::class)
class HoldrPluginTest : TestBase() {
    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()
    lateinit var pluginClasspath: List<java.io.File>
    lateinit var buildFile: java.io.File
    lateinit var androidManifestFile: java.io.File

    @org.junit.Before
    fun setup() {
        buildFile = testProjectDir.newFile("build.gradle")
        androidManifestFile = testProjectDir.newFilePath("src/main/AndroidManifest.xml")

        val pluginClasspathResource = javaClass.getResource("/plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.openStream().bufferedReader().useLines { lines ->
            lines.map(::File).toList()
        }
    }

    @Test
    fun `plugin applies to android application project`() {
        javaClass.getResource("/AndroidManifest.xml").copyTo(androidManifestFile)

        Okio.buffer(Okio.sink(buildFile))
                .writeUtf8("""
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.5.0'
    }
}

plugins {
    id 'me.tatarka.holdr2'
}

apply plugin: 'com.android.application'

android {
    compileSdkVersion 21
    buildToolsVersion "21.1.2"

    defaultConfig {
        applicationId "me.tatarka.sample"
        minSdkVersion 8
        targetSdkVersion 21
    }
}
                """).close()

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assembleDebug", "--stacktrace")
                .withGradleVersion("2.8")
                .withPluginClasspath(pluginClasspath)
                .build()


        result.task(":assembleDebug").outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `plugin modifies layout resource file`() {
        javaClass.getResource("/AndroidManifest.xml").copyTo(androidManifestFile)
        javaClass.getResource("/layout1.xml").copyTo(testProjectDir.newFilePath("src/main/res/layout/layout1.xml"))

        Okio.buffer(Okio.sink(buildFile))
                .writeUtf8("""
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.5.0'
    }
}

plugins {
    id 'me.tatarka.holdr2'
}

apply plugin: 'com.android.application'

android {
    compileSdkVersion 21
    buildToolsVersion "21.1.2"

    defaultConfig {
        applicationId "me.tatarka.sample"
        minSdkVersion 8
        targetSdkVersion 21
    }
}
                """).close()

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assembleDebug")
                .withGradleVersion("2.8")
                .withPluginClasspath(pluginClasspath)
                .build()


        result.task(":assembleDebug").outcome shouldBe TaskOutcome.SUCCESS

        val layout = File(testProjectDir.root, "build/intermediates/res/merged/debug/layout/layout1.xml")
        layout should exist()

        String(layout.readBytes()).contains("<layout>") shouldBe false

        // Running a build again should show as nothing changed.
        val result2 = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assembleDebug")
                .withGradleVersion("2.8")
                .withPluginClasspath(pluginClasspath)
                .build()

        result2.task(":assembleDebug").outcome shouldBe TaskOutcome.UP_TO_DATE
    }

    @Test
    fun `plugin generates holdr class`() {
        javaClass.getResource("/AndroidManifest.xml").copyTo(androidManifestFile)
        javaClass.getResource("/layout1.xml").copyTo(testProjectDir.newFilePath("src/main/res/layout/layout1.xml"))

        Okio.buffer(Okio.sink(buildFile))
                .writeUtf8("""
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.5.0'
    }
}

plugins {
    id 'me.tatarka.holdr2'
}

apply plugin: 'com.android.application'

android {
    compileSdkVersion 21
    buildToolsVersion "21.1.2"

    defaultConfig {
        applicationId "me.tatarka.sample"
        minSdkVersion 8
        targetSdkVersion 21
    }
}
                """).close()

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assembleDebug", "--stacktrace")
                .withGradleVersion("2.8")
                .withPluginClasspath(pluginClasspath)
                .build()


        result.task(":assembleDebug").outcome shouldBe TaskOutcome.SUCCESS

        val output = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout1.java")
        output should exist()
    }

    @Test
    fun `plugin handles incremental changes to layouts`() {
        javaClass.getResource("/AndroidManifest.xml").copyTo(androidManifestFile)
        javaClass.getResource("/layout1.xml").copyTo(testProjectDir.newFilePath("src/main/res/layout/layout1.xml"))

        Okio.buffer(Okio.sink(buildFile))
                .writeUtf8("""
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.5.0'
    }
}

plugins {
    id 'me.tatarka.holdr2'
}

apply plugin: 'com.android.application'

android {
    compileSdkVersion 21
    buildToolsVersion "21.1.2"

    defaultConfig {
        applicationId "me.tatarka.sample"
        minSdkVersion 8
        targetSdkVersion 21
    }
}
                """).close()

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assembleDebug")
                .withGradleVersion("2.8")
                .withPluginClasspath(pluginClasspath)
                .build()

        println(result.output)

        val layout2File = testProjectDir.newFilePath("src/main/res/layout/layout2.xml")

        fun createNewLayout() {
            javaClass.getResource("/layout2.xml").copyTo(layout2File)

            val result = GradleRunner.create()
                    .withProjectDir(testProjectDir.root)
                    .withArguments("assembleDebug")
                    .withGradleVersion("2.8")
                    .withPluginClasspath(pluginClasspath)
                    .build()

            println(result.output)

            result.task(":assembleDebug").outcome shouldBe TaskOutcome.SUCCESS

            val output1 = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout1.java")
            output1 should exist()
            val output2 = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout2.java")
            output2 should exist()
        }
        createNewLayout()

        fun deleteLayout() {
            layout2File.delete()

            val result = GradleRunner.create()
                    .withProjectDir(testProjectDir.root)
                    .withArguments("assembleDebug")
                    .withGradleVersion("2.8")
                    .withPluginClasspath(pluginClasspath)
                    .build()

            println(result.output)

            result.task(":assembleDebug").outcome shouldBe TaskOutcome.SUCCESS
            val output1 = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout1.java")
            output1 should exist()
            val output2 = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout2.java")
            output2 should notExist()
        }
        deleteLayout()
    }
}

