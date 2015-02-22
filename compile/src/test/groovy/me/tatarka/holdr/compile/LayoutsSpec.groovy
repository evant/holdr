package me.tatarka.holdr.compile

import me.tatarka.holdr.model.Include
import me.tatarka.holdr.model.Layout
import me.tatarka.holdr.model.Listener
import me.tatarka.holdr.model.View
import spock.lang.Specification

/**
 * Created by evan on 8/24/14.
 */
public class LayoutsSpec extends Specification {
    def "a single view merges to that view"() {
        expect:
        Layout.of("test").view(View.of("test", "id")).build().refs == [View.of("test", "id").build()]
    }

    def "2 views with the same type merge to that type"() {
        expect:
        Layout.merge(
                Layout.of("test").view(View.of("test", "id")),
                Layout.of("test").view(View.of("test", "id"))
        ).refs == [View.of("test", "id").build()]
    }

    def "2 views with different types merge to View"() {
        expect:
        Layout.merge(
                Layout.of("test").view(View.of("test1", "id")),
                Layout.of("test").view(View.of("test2", "id"))
        ).refs == [View.of("android.view.View", "id").build()]
    }

    def "2 includes with the same layout merge"() {
        expect:
        Layout.merge(
                Layout.of("test").include(Include.of("test", "id")),
                Layout.of("test").include(Include.of("test", "id"))
        ).refs == [Include.of("test", "id").build()]
    }

    def "2 includes with different layouts throws an exception"() {
        when:
        Layout.merge(
                Layout.of("test").include(Include.of("test1", "id")),
                Layout.of("test").include(Include.of("test2", "id"))
        )

        then:
        thrown(IllegalArgumentException)
    }

    def "a view and an include throws an exception"() {
        when:
        Layout.merge(
                Layout.of("test").view(View.of("test", "id")),
                Layout.of("test").include(Include.of("test", "id"))
        )

        then:
        thrown(IllegalArgumentException)
    }

    def "a view included in one layout and not another merges to the view but nullable"() {
        expect:
        Layout.merge(
                Layout.of("test").view(View.of("test", "id")), Layout.of("test")
        ).refs == [View.of("test", "id").nullable().build()]
    }

    def "(reversed) a view included in one layout and not another merges to the view but nullable"() {
        expect:
        Layout.merge(
                Layout.of("test"), Layout.of("test").view(View.of("test", "id"))
        ).refs == [View.of("test", "id").nullable().build()]
    }

    def "a layout with a custom superclass should be used over a layout without one"() {
        expect:
        Layout.merge(
                Layout.of("test").superclass("test.TestHoldr"), Layout.of("test")
        ).superclass == "test.TestHoldr"
    }

    def "(reversed) a layout with a custom superclass should be used over a layout without one"() {
        expect:
        Layout.merge(
                Layout.of("test"), Layout.of("test").superclass("test.TestHoldr")
        ).superclass == "test.TestHoldr"
    }

    def "a layout with a custom field name keeps it when merging with one without"() {
        expect:
        Layout.merge(
                Layout.of("test").view(View.of("test", "id").fieldName("my_test")),
                Layout.of("test").view(View.of("test", "id"))
        ).refs == [View.of("test", "id").fieldName("my_test").build()]
    }

    def "(reversed) a layout with a custom field name keeps it when merging with one without"() {
        expect:
        Layout.merge(
                Layout.of("test").view(View.of("test", "id")),
                Layout.of("test").view(View.of("test", "id").fieldName("my_test"))
        ).refs == [View.of("test", "id").fieldName("my_test").build()]
    }

    def "a layout with listeners keeps them when merging with one without"() {
        expect:
        Layout.merge(
                Layout.of("test").view(View.of("test", "id").listener(Listener.Type.ON_CLICK)),
                Layout.of("test").view(View.of("test", "id"))
        ).refs == [View.of("test", "id").listener(Listener.Type.ON_CLICK).build()]
    }

    def "(reversed) a layout with listeners keeps them when merging with one without"() {
        expect:
        Layout.merge(
                Layout.of("test").view(View.of("test", "id")),
                Layout.of("test").view(View.of("test", "id").listener(Listener.Type.ON_CLICK))
        ).refs == [View.of("test", "id").listener(Listener.Type.ON_CLICK).build()]
    }
}
