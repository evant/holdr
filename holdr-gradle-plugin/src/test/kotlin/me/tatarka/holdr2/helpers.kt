package me.tatarka.holdr2

import io.kotlintest.matchers.Matcher
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

fun TemporaryFolder.newFilePath(path: String): File {
    try {
        val paths: List<String> = path.split("/")
        newFolder(*paths.slice(0..paths.size - 2).toTypedArray())
    } catch (e: IOException) {
        // folder might already exist
    }
    return newFile(path)
}

fun URL.copyTo(output: File) {
    openStream().use { input ->
        FileOutputStream(output).use { out ->
            input.copyTo(out)
        }
    }
}

fun exist(): Exist = Exist()

fun notExist(): NotExist = NotExist()

class Exist : Matcher<File> {
    override fun test(value: File) {
        if (!value.exists()) {
            throw AssertionError(value.path + " does not exist")
        }
    }
}

class NotExist : Matcher<File> {
    override fun test(value: File) {
        if (value.exists()) {
            throw AssertionError(value.path + " exists")
        }
    }
}
