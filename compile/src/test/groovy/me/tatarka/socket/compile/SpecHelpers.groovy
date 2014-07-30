package me.tatarka.socket.compile

import groovy.xml.MarkupBuilder

class SpecHelpers {
    public static def xml(Closure<MarkupBuilder> f) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        f(xml)
        new StringReader(writer.toString())
    }

    public static def code(SocketGenerator generator, String layoutName, List<View> views) {
        StringWriter writer = new StringWriter()
        generator.generate(layoutName, views, writer)
        writer.toString()
    }
}
