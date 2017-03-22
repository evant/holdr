package me.tatarka.holdr2

import com.tickaroo.tikxml.XmlReader
import com.tickaroo.tikxml.XmlWriter
import okio.Buffer
import okio.BufferedSink
import okio.ByteString

private val END_LAYOUT_TAG = ByteString.encodeUtf8("</layout>")

fun tagName(fileName: String, layout: Layout): String =
        layout.variant + "/" + fileName.removeSuffix(".xml")


fun stripLayout(fileName: String, source: String, out: Buffer): StripResult =
        stripLayout(fileName, Layout("layout", Buffer().writeUtf8(source)), { out })

fun stripLayout(fileName: String, layout: Layout, outF: () -> BufferedSink): StripResult {
    val reader = XmlReader.of(layout.source)
    reader.beginElement()
    val rootName = reader.nextElementName()
    val attributes = arrayListOf<Pair<String, String>>()
    while (reader.hasAttribute()) {
        val name = reader.nextAttributeName()
        val value = reader.nextAttributeValue()
        attributes.add(Pair(name, value))
    }
    if (rootName != "layout") {
        if (attributes.find { it == Pair("android:tag", tagName(fileName, layout)) } != null) {
            return StripResult.ALREADY_STRIPPED
        } else {
            return StripResult.SKIPPED
        }
    }
    reader.beginElement()
    val rootTag = reader.nextElementName()
    while (reader.hasAttribute()) {
        val name = reader.nextAttributeName()
        val value = reader.nextAttributeValue()
        attributes.add(Pair(name, value))
    }

    if (attributes.find { it.first == "xmlns:android" } == null) {
        attributes.add(0, Pair("xmlns:android", "http://schemas.android.com/apk/res/android"))
    }

    attributes.add(Pair("android:tag", tagName(fileName, layout)))

    outF().use { out ->
        val writer = XmlWriter.of(out)

        writer.beginElement(rootTag)
        for ((name, value) in attributes) {
            writer.attribute(name, value)
        }

        // We don't actually need to parse the rest of the document, just slurp up until the closing
        // xml tag copy it out.

        // fixup consumed markup tokens
        when (reader.peek()) {
            XmlReader.XmlToken.ELEMENT_BEGIN -> {
                out.writeByte('>'.toInt())
                out.writeByte('<'.toInt())
            }
            XmlReader.XmlToken.ELEMENT_END -> {
                out.writeByte('/'.toInt())
                out.writeByte('>'.toInt())
            }
        }

        val buffer = Buffer()
        while (true) {
            val bytes = layout.source.read(buffer, 4096)
            if (bytes == -1L) {
                break
            }
            val endTag = buffer.indexOf(END_LAYOUT_TAG)
            if (endTag >= 0) {
                // Found the end layout tag
                out.write(buffer, endTag)
                break
            } else {
                // No end layout tag
                out.writeAll(buffer)
            }
        }
    }
    return StripResult.STRIPPED
}

enum class StripResult {
    /**
     * The processor stripped the file.
     */
    STRIPPED,
    /**
     * The file was skipped because it did not have a root <layout> tag
     */
    SKIPPED,
    /**
     * The file was skipped because it had already been stripped.
     */
    ALREADY_STRIPPED
}
