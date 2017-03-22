package me.tatarka.holdr2.assertions

import me.tatarka.assertk.Assert
import me.tatarka.assertk.assert
import me.tatarka.assertk.assertions.isEqualTo
import me.tatarka.assertk.assertions.support.expected
import okio.Buffer

fun Assert<Buffer>.isEmpty() {
    if (actual.size() == 0L) return
    expected("to be empty but has size:${actual.size()}")
}

fun Assert<Buffer>.hasUtf8(text: String) {
    assert("utf8", actual.clone().readUtf8()).isEqualTo(text)
}

