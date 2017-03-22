package me.tatarka.holdr2.plugin

import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.ProjectUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.jps.incremental.storage.FileKeyDescriptor
import java.io.DataInput
import java.io.DataOutput
import java.io.File
import java.util.*

class LayoutIndexExtension : FileBasedIndexExtension<File, Layout>() {

    val NAME = ID.create<File, Layout>("me.tatarka.holdr.layouts.index")!!
    private val ID_REGEX = "@\\+?(android:)?id/".toRegex()

    override fun getName(): ID<File, Layout> = NAME

    override fun getVersion(): Int = 1

    override fun dependsOnFileContent(): Boolean = true

    override fun getKeyDescriptor(): KeyDescriptor<File> = FileKeyDescriptor()

    override fun getInputFilter(): FileBasedIndex.InputFilter =
            object : DefaultFileTypeSpecificInputFilter(StdFileTypes.XML) {
                override fun acceptInput(file: VirtualFile): Boolean {
                    val parent = file.parent
                    if (parent == null || !parent.name.startsWith("layout")) {
                        return false
                    }
                    val project = ProjectUtil.guessProjectForFile(file) ?: return false
                    val module = ModuleUtilCore.findModuleForFile(file, project) ?: return false
                    val androidFacet = AndroidFacet.getInstance(module) ?: return false
                    return androidFacet.allResourceDirectories.any { it == parent.parent }
                }
            }

    override fun getValueExternalizer(): DataExternalizer<Layout> =
            object : DataExternalizer<Layout> {
                override fun save(output: DataOutput, value: Layout) {
                    output.writeInt(value.refs.size)
                    for ((type, name) in value.refs) {
                        output.writeUTF(type)
                        output.writeUTF(name)
                    }
                }

                override fun read(input: DataInput): Layout {
                    val size = input.readInt()
                    val refs = ArrayList<Ref>(size)
                    for (i in 0..size - 1) {
                        refs.add(Ref(type = input.readUTF(), name = input.readUTF()))
                    }
                    return Layout(refs)
                }
            }

    override fun getIndexer(): DataIndexer<File, Layout, FileContent> = DataIndexer { inputData ->
        var seenLayoutRoot = false
        val refs = ArrayList<Ref>()

        inputData.psiFile.accept(object : XmlRecursiveElementVisitor() {
            override fun visitXmlTag(tag: XmlTag) {
                if (!seenLayoutRoot) {
                    if (tag.name != "layout") {
                        // not a layout root, skip
                        return
                    }
                    seenLayoutRoot = true
                } else {
                    for (attribute in tag.attributes) {
                        val value = attribute.value
                        if (attribute.name == "android:id" && value != null) {
                            refs.add(Ref(type = expandPath(tag.name), name = nameFromId(value)))
                            break
                        }
                    }
                }
                super.visitXmlTag(tag)
            }
        })

        if (!seenLayoutRoot) {
            emptyMap()
        } else {
            mapOf(File(inputData.fileName) to Layout(refs))
        }
    }

    private fun nameFromId(id: String): String
            = ID_REGEX.replaceFirst(id, if (id.startsWith("@android")) "android_" else "")

    private fun expandPath(name: String): String {
        if (name.contains('.')) {
            return name
        }
        return (if (name == "WebView") "android.webkit." else "android.widget.") + name
    }
}