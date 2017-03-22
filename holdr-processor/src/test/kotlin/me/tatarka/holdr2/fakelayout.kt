package me.tatarka.holdr2

import android.view.View
import android.view.ViewGroup
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.Mockito.`when`

fun <V : View> view(type: Class<V>, tag: String? = null): V {
    val view = Mockito.mock(type)
    `when`(view.tag).thenReturn(tag)
    return view
}

fun <V : ViewGroup> view(type: Class<V>, vararg children: View, tag: String? = null): View {
    val viewGroup = Mockito.mock(type)
    `when`(viewGroup.tag).thenReturn(tag)
    `when`(viewGroup.childCount).thenReturn(children.size)
    `when`(viewGroup.getChildAt(anyInt())).thenAnswer { input -> children[input.arguments[0] as Int] }
    return viewGroup
}
