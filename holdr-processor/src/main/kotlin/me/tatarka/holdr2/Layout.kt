package me.tatarka.holdr2

import okio.Buffer
import okio.BufferedSource

fun Layout(variant: String, source: String): Layout =
        Layout(variant, Buffer().writeUtf8(source))

class Layout(val variant: String, val source: BufferedSource)
