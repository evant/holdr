package me.tatarka.holdr2

import android.support.constraint.ConstraintLayout
import android.widget.TextView
import io.kotlintest.TestBase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ExampleProcessorTest : TestBase() {

    lateinit var processor: Processor

    @Before
    fun setup() {
        processor = Processor("packageName")
    }

    @Test
    fun `constraint layout with text view`() {
        val source = """<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <android.support.constraint.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context="me.tatarka.holdr2.MainActivity">

        <TextView
            android:id="@+id/text"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginEnd="8dp"
            android:layout_marginLeft="8dp"
            android:layout_marginRight="8dp"
            android:layout_marginStart="8dp"
            android:layout_marginTop="8dp"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:layout_constraintLeft_creator="1"
            tools:layout_constraintRight_creator="1"
            tools:layout_constraintTop_creator="1"
            tools:text="Text" />
    </android.support.constraint.ConstraintLayout>
</layout>"""
        val text = view(TextView::class.java)
        val out = processor.process(layoutFile(), source)
        val code = compile(processor.className(layoutFile()), out)
                .new(view(ConstraintLayout::class.java, text, tag = tagName()))
        code.field("text") shouldBe text
    }
}

