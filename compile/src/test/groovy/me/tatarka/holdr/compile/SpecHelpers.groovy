package me.tatarka.holdr.compile

import groovy.xml.MarkupBuilder

class SpecHelpers {
    public static def xml(Closure<MarkupBuilder> f) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        f(xml)
        new StringReader(writer.toString())
    }

    public static def code(HoldrGenerator generator, String layoutName, Set<Ref> refs) {
        StringWriter writer = new StringWriter()
        generator.generate(layoutName, refs, writer)
        writer.toString()
    }
}