package me.tatarka.holdr2

import android.content.Context
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import io.kotlintest.TestBase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class SimpleBindingTest : TestBase() {

    lateinit var processor: Processor

    @Before
    fun setup() {
        processor = Processor("packageName")
    }

    @Test
    fun `empty layout creates empty class`() {
        val source = """
        <layout>
            <FrameLayout/>
        </layout>
        """
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .publicFieldCount shouldBe 0
    }

    @Test
    fun `layout with a single view with id`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/text"/>
        </layout>
        """
        val layout = view(TextView::class.java, tagName())
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .new(layout)
                .field("text") shouldBe layout
    }

    @Test
    fun `layout with a single view with android id`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@android:id/text"/>
        </layout>
        """
        val layout = view(TextView::class.java, tagName())
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .new(layout)
                .field("android_text") shouldBe layout
    }

    @Test
    fun `layout with a single webkit view with id`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <WebView android:id="@+id/webview"/>
        </layout>
        """
        val layout = view(WebView::class.java, tagName())
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .new(layout)
                .field("webview") shouldBe layout
    }

    @Test
    fun `layout with a single custom view with id`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <me.tatarka.holdr2.CustomView android:id="@+id/custom"/>
        </layout>
        """
        val layout = view(CustomView::class.java, tagName())
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .new(layout)
                .field("custom") shouldBe layout
    }

    @Test
    fun `layout with 2 views with ids`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <LinearLayout>
                <TextView android:id="@+id/text1"/>
                <TextView android:id="@+id/text2"/>
            </LinearLayout>
        </layout>
        """
        val text1 = view(TextView::class.java)
        val text2 = view(TextView::class.java)
        val out = processor.process(layoutFile(), source)
        val code = compile(processor.className(layoutFile()), out)
                .new(view(LinearLayout::class.java, text1, text2, tag = tagName()))
        code.field("text1") shouldBe text1
        code.field("text2") shouldBe text2
    }

    @Test
    fun `layout with nested views with ids`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <LinearLayout android:id="@+id/layout">
                    <TextView android:id="@+id/text1"/>
                    <TextView android:id="@+id/text2"/>
                </LinearLayout>
            </FrameLayout>
        </layout>
        """
        val text1 = view(TextView::class.java)
        val text2 = view(TextView::class.java)
        val layout = view(LinearLayout::class.java, text1, text2)
        val out = processor.process(layoutFile(), source)
        val code = compile(processor.className(layoutFile()), out)
                .new(view(FrameLayout::class.java, layout, tag = tagName()))
        code.field("layout") shouldBe layout
        code.field("text1") shouldBe text1
        code.field("text2") shouldBe text2
    }

    @Test
    fun `layout with views with ids inside views without ids`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <LinearLayout>
                    <TextView android:id="@+id/text1"/>
                    <TextView android:id="@+id/text2"/>
                </LinearLayout>
            </FrameLayout>
        </layout>
        """
        val text1 = view(TextView::class.java)
        val text2 = view(TextView::class.java)
        val out = processor.process(layoutFile(), source)
        val code = compile(processor.className(layoutFile()), out)
                .new(view(FrameLayout::class.java, view(LinearLayout::class.java, text1, text2), tag = tagName()))
        code.field("text1") shouldBe text1
        code.field("text2") shouldBe text2
    }

    @Test
    fun `layout with view with id at one level and other one level deeper`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <TextView android:id="@+id/text1"/>
                <LinearLayout>
                    <TextView android:id="@+id/text2"/>
                </LinearLayout>
            </FrameLayout>
        </layout>
        """
        val text1 = view(TextView::class.java)
        val text2 = view(TextView::class.java)
        val out = processor.process(layoutFile(), source)
        val code = compile(processor.className(layoutFile()), out)
                .new(view(FrameLayout::class.java, text1, view(LinearLayout::class.java, text2), tag = tagName()))
        code.field("text1") shouldBe text1
        code.field("text2") shouldBe text2
    }

    @Test
    fun `layout with view with id at one level and other one level higher`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <LinearLayout>
                    <TextView android:id="@+id/text1"/>
                </LinearLayout>
                <TextView android:id="@+id/text2"/>
            </FrameLayout>
        </layout>
        """
        val text1 = view(TextView::class.java)
        val text2 = view(TextView::class.java)
        val out = processor.process(layoutFile(), source)
        val code = compile(processor.className(layoutFile()), out)
                .new(view(FrameLayout::class.java, view(LinearLayout::class.java, text1), text2, tag = tagName()))
        code.field("text1") shouldBe text1
        code.field("text2") shouldBe text2
    }

    @Test
    fun `layout with fragment is ignored`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <fragment android:id="@+id/frag"/>
        </layout>
        """
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .publicFieldCount shouldBe 0
    }

    @Test
    fun `layout with include with id`() {
        val source1 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/text"/>
        </layout>
        """
        val source2 = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <include layout="@layout/${layoutName(1)}" android:id="@+id/include"/>
            </FrameLayout>
        </layout>
        """
        val text = view(TextView::class.java, tagName(suffix = 1))
        val out1 = processor.process(layoutFile(1), source1)
        val out2 = processor.process(layoutFile(2), source2)
        compile(processor.className(layoutFile(1)), out1)
        val code = compile(processor.className(layoutFile(2)), out2)
                .new(view(FrameLayout::class.java, text, tag = tagName(suffix = 2)))

        code.field("include")!!.javaClass.simpleName shouldBe layoutName(1)
        code.field("include.text") shouldBe text
    }

    @Test
    fun `layout with root id and additional attributes`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <TextView android:id="@+id/text"
            android:layout_width="match_parent"/>
        </layout>
        """
        val layout = view(TextView::class.java, tagName())
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .new(layout)
                .field("text") shouldBe layout
    }

    @Test
    fun `layout with child id and additional attributes`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <TextView android:id="@+id/text"
                android:layout_width="match_parent"/>
            </FrameLayout>
        </layout>
        """
        val text = view(TextView::class.java)
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .new(view(FrameLayout::class.java, text, tag = tagName()))
                .field("text") shouldBe text
    }

    @Test
    fun `has getRoot that returns root view`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <TextView android:id="@+id/text"/>
            </FrameLayout>
        </layout>
        """
        val text = view(TextView::class.java)
        val layout = view(FrameLayout::class.java, text, tag = tagName())
        val out = processor.process(layoutFile(), source)
        compile(processor.className(layoutFile()), out)
                .new(layout)
                .call("getRoot") shouldBe layout
    }

    @Test
    fun `wrong layout throws exception`() {
        val source = """
        <layout xmlns:android="http://schemas.android.com/apk/res/android">
            <FrameLayout>
                <TextView android:id="@+id/text"/>
            </FrameLayout>
        </layout>
        """
        val out = processor.process(layoutFile(), source)
        shouldThrow<IllegalArgumentException> {
            compile(processor.className(layoutFile()), out)
                    .new(view(FrameLayout::class.java))
        }
    }
}

open class CustomView(context: Context) : View(context)
