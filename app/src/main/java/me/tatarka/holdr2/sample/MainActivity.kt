package me.tatarka.holdr2.sample

import android.app.Activity
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.ViewGroup
import me.tatarka.holdr2.LayoutUtil
import me.tatarka.holdr2.sample.holdr.activity_main

class MainActivity : AppCompatActivity() {

    lateinit var layout: activity_main

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = setContentViewLayout(R.layout.activity_main)
        if (layout.text != null) {
            layout.text.text = "Holdr!"
        } else {
            layout.text_land.text = "Holdr Land!"
        }
    }
}


fun <L> Activity.setContentViewLayout(@LayoutRes layout: Int): L
        = LayoutUtil.setContentView(this, layout)

fun <L> LayoutInflater.inflateLayout(@LayoutRes layout: Int, parent: ViewGroup?, attachToRoot: Boolean = parent != null): L
        = LayoutUtil.inflate(this, layout, parent, attachToRoot)