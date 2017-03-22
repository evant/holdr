package me.tatarka.holdr2.plugin

data class Ref(val type: String, val name: String)

data class Layout(val refs: List<Ref>)