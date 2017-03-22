package me.tatarka.holdr2

import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import io.kotlintest.TestBase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MergeProcessorTest : TestBase() {

    lateinit var processor: Processor

    @Before
    fun setup() {
        processor = Processor("packageName")
    }

    @Test
    fun `layouts with single matching views with ids`() {
        val source1 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/text"/>
        </layout>
        """
        val source2 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/text"/>
        </layout>
        """
        val layout1 = view(TextView::class.java, tagName())
        val layout2 = view(TextView:: class.java, tagName("layout-land"))
        val out = processor.process(layoutFile(), listOf(
                Layout("layout", source1),
                Layout("layout-land", source2)))
        val compiledClass = compile(processor.className(layoutName()), out)
        compiledClass.field("text").type shouldBe TextView::class.java

        compiledClass.new(layout1)
                .field("text") shouldBe layout1
        compiledClass.new(layout2)
                .field("text") shouldBe layout2
    }

    @Test
    fun `layout with single views with different ids`() {
        val source1 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/text1"/>
        </layout>
        """
        val source2 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/text2"/>
        </layout>
        """
        val layout1 = view(TextView::class.java, tagName())
        val layout2 = view(TextView:: class.java, tagName("layout-land"))
        val out = processor.process(layoutFile(), listOf(
                Layout("layout", source1),
                Layout("layout-land", source2)))
        val compiledClass = compile(processor.className(layoutName()), out)

        compiledClass.new(layout1).let {
            it.field("text1") shouldBe layout1
            it.field("text2") shouldBe null
        }
        compiledClass.new(layout2).let {
            it.field("text1") shouldBe null
            it.field("text2") shouldBe layout2
        }
    }

    @Test
    fun `layout with views with same ids in different locations`() {
        val source1 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/text"/>
        </layout>
        """
        val source2 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <TextView android:id="@+id/text"/>
            </FrameLayout>
        </layout>
        """
        val text1 = view(TextView::class.java, tagName())
        val text2 = view(TextView:: class.java)
        val out = processor.process(layoutFile(), listOf(
                Layout("layout", source1),
                Layout("layout-land", source2)))
        val compiledClass = compile(processor.className(layoutName()), out)

        compiledClass.new(text1).field("text") shouldBe text1
        compiledClass.new(view(FrameLayout::class.java, text2, tag = tagName("layout-land")))
                .field("text") shouldBe text2
    }

    @Test
    fun `layout with views with same ids and different types`() {
        val source1 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/view"/>
        </layout>
        """
        val source2 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <ImageView android:id="@+id/view"/>
        </layout>
        """
        val layout1 = view(TextView::class.java, tagName())
        val layout2 = view(ImageView:: class.java, tagName("layout-land"))
        val out = processor.process(layoutFile(), listOf(
                Layout("layout", source1),
                Layout("layout-land", source2)))
        val compiledClass = compile(processor.className(layoutName()), out)

        compiledClass.new(layout1).field("view") shouldBe layout1
        compiledClass.new(layout2).field("view") shouldBe layout2
    }
}

