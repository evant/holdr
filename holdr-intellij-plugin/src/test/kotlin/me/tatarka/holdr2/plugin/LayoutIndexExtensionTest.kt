package me.tatarka.holdr2.plugin

import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.indexing.FileContentImpl
import java.io.*
import java.nio.charset.Charset

class LayoutIndexExtensionTest : LightPlatformCodeInsightFixtureTestCase() {

    lateinit var layoutIndexExt: LayoutIndexExtension

    override fun setUp() {
        super.setUp()
        layoutIndexExt = LayoutIndexExtension()
    }

    fun `test indexer creates layout when layout has layout tags`() {
        val indexer = layoutIndexExt.indexer
        val content = "<layout><TextView/></layout>"
        val fileContent = FileContentImpl(
                LightVirtualFile("layout.xml", StdFileTypes.XML, content),
                content,
                Charset.forName("UTF-8")
        )
        val result = indexer.map(fileContent)


        assertEquals(Layout(emptyList()), result[File("layout.xml")])
    }

    fun `test indexer includes ref when layout has layout tags and id`() {
        val indexer = layoutIndexExt.indexer
        val content = "<layout><TextView xmlns:android=\"http://schemas.android.com/apk/res/android\" android:id=\"@+id/test\"/></layout>"
        val fileContent = FileContentImpl(
                LightVirtualFile("layout.xml", StdFileTypes.XML, content),
                content,
                Charset.forName("UTF-8")
        )
        val result = indexer.map(fileContent)


        assertEquals(Layout(listOf(Ref(type = "android.widget.TextView", name = "test"))), result[File("layout.xml")])
    }

    fun `test value externalizer roundtrips layout correctly`() {
        val externalizer = layoutIndexExt.valueExternalizer
        val output = ByteArrayOutputStream()
        externalizer.save(DataOutputStream(output), Layout(listOf(Ref(type = "type", name = "name"))))
        output.flush()
        val input = ByteArrayInputStream(output.toByteArray())
        val layout = externalizer.read(DataInputStream(input))

        assertEquals(Layout(listOf(Ref(type = "type", name = "name"))), layout)
    }
}
