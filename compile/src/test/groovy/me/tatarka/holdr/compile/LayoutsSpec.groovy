package me.tatarka.holdr.compile

import spock.lang.Specification

import static me.tatarka.holdr.compile.SpecHelpers.layout
import static me.tatarka.holdr.compile.SpecHelpers.layoutRefs

/**
 * Created by evan on 8/24/14.
 */
public class LayoutsSpec extends Specification {
    def "a single view merges to that view"() {
        expect:
        layoutRefs(
                new ParsedLayout([View.of("test", "id").build()])
        ) == [View.of("test", "id").build()]
    }
    
    def "2 views with the same type merge to that type"() {
        expect:
        layoutRefs(
                new ParsedLayout([View.of("test", "id").build()]),
                new ParsedLayout([View.of("test", "id").build()])
        ) == [View.of("test", "id").build()]
    }
    
    def "2 views with different types merge to View"() {
        expect:
        layoutRefs(
                new ParsedLayout([View.of("test1", "id").build()]),
                new ParsedLayout([View.of("test2", "id").build()])
        ) == [View.of("android.view.View", "id").build()]
    }
    
    def "2 includes with the same layout merge"() {
        expect:
        layoutRefs(
                new ParsedLayout([Include.of("test", "id").build()]),
                new ParsedLayout([Include.of("test", "id").build()])
        ) == [Include.of("test", "id").build()]
    }
    
    def "2 includes with different layouts throws an exception"() {
        when:
        layoutRefs(
                new ParsedLayout([Include.of("test1", "id").build()]),
                new ParsedLayout([Include.of("test2", "id").build()])
        )
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "a view and an include throws an exception"() {
        when:
        layoutRefs(
                new ParsedLayout([View.of("test", "id").build()]),
                new ParsedLayout([Include.of("test", "id").build()])
        )

        then:
        thrown(IllegalArgumentException)
    }
    
    def "a view included in one layout and not another merges to the view but nullable"() {
        expect:
        layoutRefs(
                new ParsedLayout([View.of("test", "id").build()]), new ParsedLayout([])
        ) == [View.of("test", "id").nullable().build()]
    }

    def "(reversed) a view included in one layout and not another merges to the view but nullable"() {
        expect:
        layoutRefs(
                new ParsedLayout([]), new ParsedLayout([View.of("test", "id").build()])
        ) == [View.of("test", "id").nullable().build()]
    }
    
    def "a layout with a custom superclass should be used over a layout without one"() {
        expect:
        layout(
                new ParsedLayout("test.TestHoldr", []), new ParsedLayout([])
        ).superclass == "test.TestHoldr"
    }

    def "(reversed) a layout with a custom superclass should be used over a layout without one"() {
        expect:
        layout(
                new ParsedLayout([]), new ParsedLayout("test.TestHoldr", [])
        ).superclass == "test.TestHoldr"
    }
}
