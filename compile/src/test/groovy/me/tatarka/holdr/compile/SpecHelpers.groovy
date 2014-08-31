package me.tatarka.holdr.compile

import groovy.xml.MarkupBuilder

class SpecHelpers {
    public static StringReader xml(Closure<MarkupBuilder> f) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        f(xml)
        new StringReader(writer.toString())
    }

    public static String code(HoldrGenerator generator, String layoutName, Set<Ref> refs) {
        StringWriter writer = new StringWriter()
        generator.generate(layoutName, refs, writer)
        writer.toString()
    }
    
    public static List<Ref> layouts(Collection<Ref>... refCollection) {
        Layouts layouts = new Layouts()
        refCollection.eachWithIndex { refs, i ->
            layouts.add(new File("layout-$i/my_layout.xml"), refs)
        }
        layouts.first().refs.values().collect()
    }
}
