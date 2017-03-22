package me.tatarka.holdr2

import android.view.View
import android.widget.TextView
import me.tatarka.assertk.assert
import me.tatarka.assertk.assertions.isInstanceOf
import me.tatarka.holdr2.assertions.hasReturnType
import org.junit.Test
import packageName.R

class LayoutMappingTest {

    @Test
    fun `creates layout mapping class with create method`() {
        val layoutMapping = LayoutMapping("packageName", layoutPackageName = "test1")
        val out = layoutMapping.process(emptyList())
        val method = compile("test1.LayoutMapping", out)
                .method("create", Int::class.java, View::class.java)

        assert(method).hasReturnType(Object::class)
    }

    @Test
    fun `maps layout`() {
        val layoutMapping = LayoutMapping("packageName", layoutPackageName = "test2")
        val out = layoutMapping.process(listOf("layout"))
        val layout = compile("test2.LayoutMapping", out)
                .new()
                .call("create", R.layout.layout, view(TextView::class.java))!!

        assert(layout).isInstanceOf(layout::class)
    }
}
