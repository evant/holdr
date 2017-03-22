package me.tatarka.holdr2

import com.tickaroo.tikxml.XmlReader
import com.tickaroo.tikxml.XmlWriter
import okio.*
import java.util.*
import kotlin.comparisons.compareValues

private val ID = "@\\+?(android:)?id/".toRegex()


class Processor(val packageName: String) {

    fun simpleClassName(fileName: String): String = fileName.removeSuffix(".xml")

    fun className(fileName: String): String = "$packageName.${simpleClassName(fileName)}"

    fun process(fileName: String, layout: String): String
            = process(fileName, listOf(Layout("layout", layout)))

    fun process(fileName: String, layouts: List<Layout>): String {
        val out = Buffer()
        process(fileName, layouts, out)
        return out.readUtf8()
    }

    fun process(fileName: String, layouts: List<Layout>, out: BufferedSink) {
        val className = simpleClassName(fileName)

        out.writeUtf8("package $packageName;\n\n")
        out.writeUtf8("public class $className {\n")
        out.writeUtf8("    private final android.view.View _view0;\n\n")

        val refs = ArrayList<Ref>()

        out.writeUtf8("    public $className(android.view.View _view0) {\n")
        out.writeUtf8("        this._view0 = _view0;\n")
        out.writeUtf8("        String _tag = (String) _view0.getTag();\n")
        out.writeUtf8("        if (_tag == null) {\n")
        out.writeUtf8("            throw new IllegalArgumentException(\"view is missing tag. Are you sure you passed in the correct view?\");\n")
        out.writeUtf8("        }\n")
        out.writeUtf8("        switch (_tag) {\n")
        for (layout in layouts) {
            visitLayout(fileName, layout, refs, out)
        }
        out.writeUtf8("        default:\n")
        out.writeUtf8("            throw new IllegalArgumentException(\"view has wrong tag: '\" + _tag + \"'. Are you sure you passed in the correct view?\");\n")
        out.writeUtf8("        }\n")
        out.writeUtf8("    }\n\n")

        out.writeUtf8("    public final android.view.View getRoot() {\n")
        out.writeUtf8("        return _view0;\n")
        out.writeUtf8("    }\n")

        if (refs.isNotEmpty()) {
            out.writeUtf8("\n")
        }

        for ((type, name) in refs) {
            out.writeUtf8("    public $type $name;\n")
        }

        out.writeUtf8("}")
    }

    private fun visitLayout(fileName: String, layout: Layout, refs: ArrayList<Ref>, out: BufferedSink) {
        val tagName = tagName(fileName, layout)
        out.writeUtf8("        case \"$tagName\": {\n")

        val reader = XmlReader.of(layout.source)

        reader.beginElement() // layout
        if (reader.nextElementName() != "layout") {
            throw IllegalArgumentException("file: ${layout.variant}/$fileName does not have root tag <layout/>")
        }

        while (reader.hasAttribute()) {
            reader.skipAttribute()
        }

        visitRootElement(reader, refs, out)
        out.writeUtf8("            break;\n")
        out.writeUtf8("        }\n")
    }

    private fun visitRootElement(reader: XmlReader, refs: ArrayList<Ref>, out: BufferedSink) {
        reader.beginElement()
        val name = reader.nextElementName()
        val ref = createRef(reader, name)

        if (ref != null) {
            appendRef(refs, ref)
            ref.emit("_view0", out)
        }

        while (reader.hasAttribute()) {
            reader.skipAttribute()
        }

        var i = 0
        while (reader.peek() == XmlReader.XmlToken.ELEMENT_BEGIN) {
            visitElement(reader, refs, out, depth = 1, index = i)
            i += 1
        }

        reader.endElement()
    }

    private fun visitElement(reader: XmlReader, refs: ArrayList<Ref>, out: BufferedSink,
                             parentPath: Path = Path.RootPath(), depth: Int = 0, index: Int = 0, shouldEmitPath: Boolean = true): Pair<Path, Boolean> {
        reader.beginElement()
        val name = reader.nextElementName()

        val ref = createRef(reader, name)

        while (reader.hasAttribute()) {
            reader.skipAttribute()
        }

        var shouldEmitPath = shouldEmitPath
        val parentPath = ref?.let { ref ->
            appendRef(refs, ref)
            val parentPath = if (shouldEmitPath) {
                shouldEmitPath = false
                parentPath.emit(out)
            } else {
                shouldEmitPath = true
                parentPath
            }
            ref.emit(parentPath.append(index), out)
            parentPath
        } ?: parentPath

        var path = (if (ref != null) Path.ViewPath(ref.name) else parentPath).run {
            if (shouldEmitPath) {
                append(index)
            } else {
                this
            }
        }

        var i = 0
        while (reader.peek() == XmlReader.XmlToken.ELEMENT_BEGIN) {
            val (p, hasId) = visitElement(reader, refs, out,
                    parentPath = path, depth = depth + 1, index = i, shouldEmitPath = shouldEmitPath)
            path = p
            shouldEmitPath = !hasId
            i += 1
        }

        reader.endElement()

        return Pair(parentPath, ref != null)
    }

    private fun createRef(reader: XmlReader, name: String): Ref? {
        return when (name) {
            "fragment" -> {
                while (reader.hasAttribute()) {
                    reader.skipAttribute()
                }
                null
            }
            "include" -> createInclude(reader)
            else -> createView(reader, name)
        }
    }

    private fun createInclude(reader: XmlReader): Ref? {
        var type: String? = null
        var name: String? = null

        while (reader.hasAttribute()) {
            val attrName = reader.nextAttributeName()
            if (attrName == "layout") {
                val attrValue = reader.nextAttributeValue()
                type = nameFromLayout(attrValue)
            } else if (attrName == "android:id") {
                val attrValue = reader.nextAttributeValue()
                name = nameFromId(attrValue)
            } else {
                reader.skipAttributeValue()
            }
        }
        if (type != null && name != null) {
            return Ref(type = type, name = name, isInclude = true)
        }
        return null
    }

    private fun createView(reader: XmlReader, name: String): Ref? {
        while (reader.hasAttribute()) {
            val attrName = reader.nextAttributeName()
            if (attrName == "android:id") {
                val viewType = expandPath(name)

                val attrValue = reader.nextAttributeValue()
                val viewIdName = nameFromId(attrValue)

                return Ref(type = viewType, name = viewIdName, isInclude = false)
            } else {
                reader.skipAttributeValue()
            }
        }
        return null
    }

    private fun nameFromId(id: String): String
            = ID.replaceFirst(id, if (id.startsWith("@android")) "android_" else "")

    private fun nameFromLayout(layout: String): String
            = packageName + "." + layout.removePrefix("@layout/")

    private fun expandPath(name: String): String {
        if (name.contains('.')) {
            return name
        }
        return (if (name == "WebView") "android.webkit." else "android.widget.") + name
    }
}

private fun appendRef(refs: ArrayList<Ref>, ref: Ref) {
    val currentRefIndex = refs.indexOfFirst { it.name == ref.name }
    if (currentRefIndex >= 0) {
        val currentRef = refs[currentRefIndex]
        if (currentRef.type != ref.type) {
            refs[currentRefIndex] = Ref("android.view.View", ref.name, isInclude = false)
        }
    } else {
        refs.add(ref)
    }
}

private data class Ref(val type: String, val name: String, val isInclude: Boolean) {
    fun emit(variable: Any, out: BufferedSink) {
        if (isInclude) {
            out.writeUtf8("            $name = new $type($variable);\n")
        } else {
            out.writeUtf8("            $name = ($type) $variable;\n")
        }
    }
}

private sealed class Path {

    fun append(index: Int): Path = ChildPath(this, index)

    abstract fun emit(out: BufferedSink): Path

    abstract fun increment(): Path

    class RootPath(val index: Int = 0) : Path() {
        override fun increment(): Path = RootPath(index + 1)

        override fun emit(out: BufferedSink): Path = this

        override fun toString(): String = "_view$index"
    }

    class ViewPath(val name: String) : Path() {
        override fun increment(): Path = this

        override fun emit(out: BufferedSink): Path = this

        override fun toString(): String = name
    }

    class ChildPath(val parent: Path, val index: Int) : Path() {
        override fun increment(): Path = ChildPath(parent.increment(), index)

        override fun emit(out: BufferedSink): Path {
            val newParent = parent.increment()
            out.writeUtf8("            android.view.ViewGroup $newParent = (android.view.ViewGroup)$this;\n")
            return newParent
        }

        override fun toString(): String = "((android.view.ViewGroup)$parent).getChildAt($index)"
    }
}

// @id/foo ->
