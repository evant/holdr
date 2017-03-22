package me.tatarka.holdr2

import android.view.View
import okio.Buffer
import okio.BufferedSource
import org.junit.Assert.fail
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.URI
import java.util.*
import javax.tools.*

internal class InMemoryJavaFileObject @Throws(Exception::class)
constructor(className: String, private val contents: String) : SimpleJavaFileObject(
        URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
        JavaFileObject.Kind.SOURCE) {

    @Throws(IOException::class)
    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = contents
}

fun compile(className: String, code: String): GeneratedCode = compile(className, Buffer().writeUtf8(code))

/**
 * Compiles the given string of java source, allowing you to call methods on it and assert their
 * results.
 */
fun compile(className: String, code: BufferedSource): GeneratedCode {
    // Create an in-memory Java file object
    val javaFileObject = InMemoryJavaFileObject(className, code.readUtf8())

    val compiler = ToolProvider.getSystemJavaCompiler()

    val fileManager = compiler.getStandardFileManager(null,
            null,
            null)

    val files = listOf(File("build/classes/test"))
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, files)

    val diagnostics = DiagnosticCollector<JavaFileObject>()

    val task = compiler.getTask(null,
            fileManager,
            diagnostics,
            null,
            null,
            listOf(javaFileObject))

    val success = task.call()!!

    fileManager.close()

    // If there' a compilation error, display error messages and fail the test
    if (!success) {
        val msg = StringBuilder()
        for (diagnostic in diagnostics.diagnostics) {
            msg.append("Code: ${diagnostic.code}\n")
            msg.append("Kind: ${diagnostic.kind}\n")
            msg.append("Position: ${diagnostic.position}\n")
            msg.append("Start Position: ${diagnostic.startPosition}\n")
            msg.append("End Position: ${diagnostic.endPosition}\n")
            msg.append("Source: ${diagnostic.source}\n")
            msg.append("Message: ${diagnostic.getMessage(Locale.getDefault())}\n\n")
        }

        msg.append(diagnostics.diagnostics[0].source.getCharContent(true))

        fail(msg.toString())
    } else {
        println(javaFileObject.getCharContent(true))
    }
    return GeneratedCode(className)
}

fun tagName(variant: String = "layout", suffix: Any = ""): String = variant + "/" + layoutName(suffix)

fun layoutName(suffix: Any = ""): String {
    val callingMethodName = Exception().stackTrace
            .first { it.className.endsWith("Test")}.methodName
    return callingMethodName.replace(" ", "_") + suffix
}

fun layoutFile(suffix: Any = ""): String {
    return layoutName(suffix) + ".xml"
}

class GeneratedCode(className: String) {
    private val klass = Class.forName(className)

    val publicFieldCount: Int
        get() = klass.declaredFields.filter { it.isAccessible }.size

    fun new(): GeneratedInstance {
        try {
            return GeneratedInstance(klass.newInstance())
        } catch (e: InvocationTargetException) {
            throw e.cause ?: e
        }
    }

    fun new(view: View): GeneratedInstance {
        val constructor = klass.getDeclaredConstructor(View::class.java)
        try {
            return GeneratedInstance(constructor.newInstance(view))
        } catch (e: InvocationTargetException) {
            throw e.cause ?: e
        }
    }

    fun field(name: String): Field = klass.getDeclaredField(name)

    fun method(name: String, vararg argsTypes: Class<*>): Method =
            klass.getDeclaredMethod(name, *argsTypes)
}

class GeneratedInstance(private val instance: Any?) {

    fun field(name: String): Any? {
        var result = instance
        for (n in name.split('.')) {
            result = result!!.javaClass.getDeclaredField(n).get(result)
        }
        return result
    }

    fun call(method: String): Any? {
        return instance!!.javaClass.getDeclaredMethod(method).invoke(instance)
    }

    fun call(method: String, arg1: Int, arg2: View): Any? {
        println("$method($arg1, $arg2)")

        return instance!!.javaClass.getDeclaredMethod(method, Int::class.java, View::class.java)
                .invoke(instance, arg1, arg2)
    }
}
