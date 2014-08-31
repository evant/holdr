package me.tatarka.holdr.compile

import groovy.xml.MarkupBuilder
import me.tatarka.holdr.compile.model.Ref

class SpecHelpers {
    public static StringReader xml(Closure<MarkupBuilder> f) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        f(xml)
        new StringReader(writer.toString())
    }

    public static String code(HoldrGenerator generator, String layoutName, String superclass, Set<Ref> refs) {
        StringWriter writer = new StringWriter()
        generator.generate(layoutName, superclass, refs, writer)
        writer.toString()
    }

    public static String code(HoldrGenerator generator, String layoutName, Set<Ref> refs) {
        code(generator, layoutName, HoldrGenerator.HOLDR_SUPERCLASS, refs)
    }
    
    public static Layouts.Layout layout(ParsedLayout... parsedLayouts) {
        Layouts layouts = new Layouts()
        parsedLayouts.eachWithIndex { ParsedLayout layout, int i ->
            layouts.add(new File("layout-$i/my_layout.xml"), layout)
        }
        layouts.first()
    }
    
    public static List<Ref> layoutRefs(ParsedLayout... parsedLayouts) {
        layout(parsedLayouts).refs
    }
}
