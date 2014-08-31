package me.tatarka.holdr.compile

import spock.lang.Specification

import static me.tatarka.holdr.compile.SpecHelpers.layouts

/**
 * Created by evan on 8/24/14.
 */
public class LayoutsSpec extends Specification {
    def "a single view merges to that view"() {
        expect:
        layouts(
                [View.of("test", "id").build()]
        ) == [View.of("test", "id").build()]
    }
    
    def "2 views with the same type merge to that type"() {
        expect:
        layouts(
                [View.of("test", "id").build()],
                [View.of("test", "id").build()]
        ) == [View.of("test", "id").build()]
    }
    
    def "2 views with different types merge to View"() {
        expect:
        layouts(
                [View.of("test1", "id").build()],
                [View.of("test2", "id").build()]
        ) == [View.of("android.view.View", "id").build()]
    }
    
    def "2 includes with the same layout merge"() {
        expect:
        layouts(
                [Include.of("test", "id").build()],
                [Include.of("test", "id").build()]
        ) == [Include.of("test", "id").build()]
    }
    
    def "2 includes with different layouts throws an exception"() {
        when:
        layouts(
                [Include.of("test1", "id").build()],
                [Include.of("test2", "id").build()]
        )
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "a view and an include throws an exception"() {
        when:
        layouts(
                [View.of("test", "id").build()],
                [Include.of("test", "id").build()]
        )

        then:
        thrown(IllegalArgumentException)
    }
    
    def "a view included in one layout and not another merges to the view but nullable"() {
        expect:
        layouts(
                [View.of("test", "id").build()], []
        ) == [View.of("test", "id").nullable().build()]
    }

    def "a view included in one layout (reversed) and not another merges to the view but nullable"() {
        expect:
        layouts(
                [], [View.of("test", "id").build()]
        ) == [View.of("test", "id").nullable().build()]
    }
}
