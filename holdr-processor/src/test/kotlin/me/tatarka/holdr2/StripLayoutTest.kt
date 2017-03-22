package me.tatarka.holdr2

import io.kotlintest.TestBase
import okio.Buffer
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class StripLayoutTest : TestBase() {

    @Test
    fun `skips file without layout tag`() {
        val source = "<TextView/>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        result shouldBe StripResult.SKIPPED
        out.size() shouldBe 0L
    }

    @Test
    fun `strips layout tags`() {
        val source = "<layout><TextView/></layout>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        result shouldBe StripResult.STRIPPED
        out.readUtf8() shouldBe "<TextView xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/test\"/>"
    }

    @Test
    fun `strips layout tags2`() {
        val source = "<layout><FrameLayout><TextView/></FrameLayout></layout>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        result shouldBe StripResult.STRIPPED
        out.readUtf8() shouldBe "<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/test\"><TextView/></FrameLayout>"
    }

    @Test
    fun `moves xmlns attributes to the root tag`() {
        val source = "<layout xmlns:android=\"http://schemas.android.com/apk/res/android\"><FrameLayout><TextView/></FrameLayout></layout>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        result shouldBe StripResult.STRIPPED
        out.readUtf8() shouldBe "<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/test\"><TextView/></FrameLayout>"
    }

    @Test
    fun `preserves attributes on root tag`() {
        val source = "<layout xmlns:android=\"http://schemas.android.com/apk/res/android\"><FrameLayout android:layout_width=\"match_parent\"><TextView/></FrameLayout></layout>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        result shouldBe StripResult.STRIPPED
        out.readUtf8() shouldBe "<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" android:layout_width=\"match_parent\" android:tag=\"layout/test\"><TextView/></FrameLayout>"
    }

    @Test
    fun `detects file with expected root tag as already stripped`() {
        val source = "<TextView xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/test\"/>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        result shouldBe StripResult.ALREADY_STRIPPED
        out.size() shouldBe 0L
    }
}

