package me.tatarka.holdr2

import doesNotExist
import exists
import me.tatarka.assertk.assert
import me.tatarka.assertk.assertAll
import me.tatarka.assertk.assertions.*
import me.tatarka.holdr2.assertions.isSuccess
import me.tatarka.holdr2.assertions.isUpToDate
import okio.Okio
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HoldrPluginTest {
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


        assert(result.task(":assembleDebug")).isSuccess()
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

        val layout = File(testProjectDir.root, "build/intermediates/res/merged/debug/layout/layout1.xml")

        assertAll {
            assert("task", result.task(":assembleDebug")).isSuccess()
            assert("layout", layout).exists()
            assert(String(layout.readBytes())).doesNotContain("<layout>")
        }

        // Running a build again should show as nothing changed.
        val result2 = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("assembleDebug")
                .withGradleVersion("2.8")
                .withPluginClasspath(pluginClasspath)
                .build()

        assert(result2.task(":assembleDebug")).isUpToDate()
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
        val output = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout1.java")

        assertAll {
            assert("task", result.task(":assembleDebug")).isSuccess()
            assert("output", output).exists()
        }
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
            val output1 = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout1.java")
            val output2 = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout2.java")

            println(result.output)

            assertAll {
                assert("task", result.task(":assembleDebug")).isSuccess()
                assert("output1", output1).exists()
                assert("output2", output2).exists()
            }
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
            val output1 = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout1.java")
            val output2 = File(testProjectDir.root, "build/generated/source/holdr/debug/me/tatarka/sample/holdr/layout2.java")

            println(result.output)

            assertAll {
                assert("task", result.task(":assembleDebug")).isSuccess()
                assert(output1).exists()
                assert(output2).doesNotExist()
            }
        }
        deleteLayout()
    }
}

