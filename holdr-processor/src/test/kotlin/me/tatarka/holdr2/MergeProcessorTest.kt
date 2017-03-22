package me.tatarka.holdr2

import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import me.tatarka.assertk.assert
import me.tatarka.assertk.assertAll
import me.tatarka.assertk.assertions.isEqualTo
import me.tatarka.assertk.assertions.isNull
import me.tatarka.assertk.assertions.isSameAs
import me.tatarka.holdr2.assertions.hasType
import org.junit.Before
import org.junit.Test

class MergeProcessorTest {

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
        val layout2 = view(TextView::class.java, tagName("layout-land"))
        val out = processor.process(layoutFile(), listOf(
                Layout("layout", source1),
                Layout("layout-land", source2)))
        val compiledClass = compile(processor.className(layoutName()), out)

        assertAll {
            assert("type", compiledClass.field("text")).hasType(TextView::class)

            assert("layout1.text", compiledClass.new(layout1)
                    .field("text")).isSameAs(layout1)

            assert("layout2.text", compiledClass.new(layout2)
                    .field("text")).isSameAs(layout2)
        }
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
        val layout2 = view(TextView::class.java, tagName("layout-land"))
        val out = processor.process(layoutFile(), listOf(
                Layout("layout", source1),
                Layout("layout-land", source2)))
        val compiledClass = compile(processor.className(layoutName()), out)


        assertAll {
            compiledClass.new(layout1).let {
                assert("layout1.text1", it.field("text1")).isSameAs(layout1)
                assert("layout1.text2", it.field("text2")).isNull()
            }
            compiledClass.new(layout2).let {
                assert("layout2.text1", it.field("text1")).isNull()
                assert("layout2.text2", it.field("text2")).isSameAs(layout2)
            }
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
        val text2 = view(TextView::class.java)
        val out = processor.process(layoutFile(), listOf(
                Layout("layout", source1),
                Layout("layout-land", source2)))
        val compiledClass = compile(processor.className(layoutName()), out)

        assertAll {
            assert("text1", compiledClass.new(text1).field("text")).isSameAs(text1)
            assert("text2", compiledClass.new(view(FrameLayout::class.java, text2, tag = tagName("layout-land")))
                    .field("text")).isSameAs(text2)
        }
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
        val layout2 = view(ImageView::class.java, tagName("layout-land"))
        val out = processor.process(layoutFile(), listOf(
                Layout("layout", source1),
                Layout("layout-land", source2)))
        val compiledClass = compile(processor.className(layoutName()), out)

        assertAll {
            assert("layout1.view", compiledClass.new(layout1).field("view")).isSameAs(layout1)
            assert("layout2.view", compiledClass.new(layout2).field("view")).isSameAs(layout2)
        }
    }
}

