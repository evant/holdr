package me.tatarka.holdr.compile

import groovy.xml.MarkupBuilder
import me.tatarka.holdr.compile.model.Ref

class SpecHelpers {
    public static String xml(Closure f) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        f.delegate = xml
        f()
        writer.toString()
    }
}
