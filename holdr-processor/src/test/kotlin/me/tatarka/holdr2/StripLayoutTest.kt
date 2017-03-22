package me.tatarka.holdr2

import me.tatarka.assertk.assert
import me.tatarka.assertk.assertAll
import me.tatarka.assertk.assertions.isEqualTo
import me.tatarka.holdr2.assertions.hasUtf8
import me.tatarka.holdr2.assertions.isEmpty
import okio.Buffer
import org.junit.Test

class StripLayoutTest {

    @Test
    fun `skips file without layout tag`() {
        val source = "<TextView/>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        assertAll {
            assert(result).isEqualTo(StripResult.SKIPPED)
            assert(out).isEmpty()
        }
    }

    @Test
    fun `strips layout tags`() {
        val source = "<layout><TextView/></layout>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        assertAll {
            assert(result).isEqualTo(StripResult.STRIPPED)
            assert(out).hasUtf8("<TextView xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/test\"/>")
        }
    }

    @Test
    fun `strips layout tags2`() {
        val source = "<layout><FrameLayout><TextView/></FrameLayout></layout>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        assertAll {
            assert(result).isEqualTo(StripResult.STRIPPED)
            assert(out).hasUtf8("<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/test\"><TextView/></FrameLayout>")
        }
    }

    @Test
    fun `moves xmlns attributes to the root tag`() {
        val source = "<layout xmlns:android=\"http://schemas.android.com/apk/res/android\"><FrameLayout><TextView/></FrameLayout></layout>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        assertAll {
            assert(result).isEqualTo(StripResult.STRIPPED)
            assert(out).hasUtf8("<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/test\"><TextView/></FrameLayout>")
        }
    }

    @Test
    fun `preserves attributes on root tag`() {
        val source = "<layout xmlns:android=\"http://schemas.android.com/apk/res/android\"><FrameLayout android:layout_width=\"match_parent\"><TextView/></FrameLayout></layout>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        assertAll {
            assert(result).isEqualTo(StripResult.STRIPPED)
            assert(out).hasUtf8("<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" android:layout_width=\"match_parent\" android:tag=\"layout/test\"><TextView/></FrameLayout>")
        }
    }

    @Test
    fun `detects file with expected root tag as already stripped`() {
        val source = "<TextView xmlns:android=\"http://schemas.android.com/apk/res/android\" android:tag=\"layout/test\"/>"
        val out = Buffer()
        val result = stripLayout("test.xml", source, out)

        assertAll {
            assert(result).isEqualTo(StripResult.ALREADY_STRIPPED)
            assert(out).isEmpty()
        }
    }
}

