package me.tatarka.holdr2

import android.view.View
import android.widget.TextView
import io.kotlintest.TestBase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import packageName.R
import packageName.holdr.layout

@RunWith(JUnit4::class)
class LayoutMappingTest : TestBase() {

    @Test
    fun `creates layout mapping class with create method`() {
        val layoutMapping = LayoutMapping("packageName", layoutPackageName = "test1")
        val out = layoutMapping.process(emptyList())
        compile("test1.LayoutMapping", out)
                .method("create", Int::class.java, View::class.java)
                .returnType shouldBe Object::class.java
    }

    @Test
    fun `maps layout`() {
        val layoutMapping = LayoutMapping("packageName", layoutPackageName = "test2")
        val out = layoutMapping.process(listOf("layout"))
        compile("test2.LayoutMapping", out)
                .new()
                .call("create", R.layout.layout, view(TextView::class.java))!!
                .javaClass shouldBe layout::class.java
    }
}
