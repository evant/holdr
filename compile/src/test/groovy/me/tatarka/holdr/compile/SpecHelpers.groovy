package me.tatarka.holdr.compile

import groovy.xml.MarkupBuilder
import me.tatarka.holdr.compile.model.Include
import me.tatarka.holdr.model.HoldrConfig

class SpecHelpers {
    public static String xml(Closure f) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        f.delegate = xml
        f()
        writer.toString()
    }

    public static HoldrConfig testHoldrConfig() {
        return new HoldrConfig() {
            @Override
            String getManifestPackage() {
                return "me.tatarka.test"
            }

            @Override
            String getHoldrPackage() {
                return "me.tatarka.test.holdr"
            }

            @Override
            boolean getDefaultInclude() {
                return true
            }
        }
    }
    
    public static IncludeResolver emptyIncludeResolver() {
        return new IncludeResolver() {
            @Override
            Layout resolveInclude(Include include) {
                return null
            }
        }
    }
}
