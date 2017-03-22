package me.tatarka.holdr2

import okio.Buffer
import okio.BufferedSink

class LayoutMapping(val packageName: String,
                    val holdrPackageName: String = packageName + ".holdr",
                    val layoutPackageName: String = "me.tatarka.holdr2.internal") {

    fun process(layouts: List<String>): String {
        val out = Buffer()
        process(layouts, out)
        return out.readUtf8()
    }

    fun process(layouts: List<String>, out: BufferedSink) {
        out.writeUtf8("package $layoutPackageName;\n\n")

        out.writeUtf8("public class LayoutMapping {\n")
        out.writeUtf8("    public Object create(int layoutRes, android.view.View view) {\n")
        out.writeUtf8("        switch (layoutRes) {\n")
        for (layout in layouts) {
            out.writeUtf8("        case $packageName.R.layout.$layout:\n")
            out.writeUtf8("            return new $holdrPackageName.$layout(view);\n")
        }
        out.writeUtf8("        default:\n")
        out.writeUtf8("            return null;\n")
        out.writeUtf8("        }\n")
        out.writeUtf8("    }\n")
        out.writeUtf8("}\n")
    }
}