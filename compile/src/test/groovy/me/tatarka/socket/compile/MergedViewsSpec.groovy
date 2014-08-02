package me.tatarka.socket.compile

import spock.lang.Specification;

/**
 * Created by evan on 7/31/14.
 */
public class MergedViewsSpec extends Specification {
    MergedViews mergedViews = new MergedViews()

    def "keeps views of separate layouts separate"() {
        mergedViews.add("test1", [View.of("android.widget.TextView", "text").build()])
        mergedViews.add("test2", [View.of("android.widget.TextView", "text").build()])

        expect:
        mergedViews.collect() == [
                ([View.of("android.widget.TextView", "text").build()] as Set),
                ([View.of("android.widget.TextView", "text").build()] as Set)
        ]
    }

    def "merges views with the same layout, combining same id's"() {
        mergedViews.add("test", [View.of("android.widget.TextView", "text").build()])
        mergedViews.add("test", [View.of("android.widget.TextView", "text").build()])

        expect:
        mergedViews.collect() == [
                ([View.of("android.widget.TextView", "text").build()] as Set)
        ]
    }

    def "merges views with the same layout, keeping all different id's"() {
        mergedViews.add("test", [View.of("android.widget.TextView", "text1").build()])
        mergedViews.add("test", [View.of("android.widget.TextView", "text2").build()])

        expect:
        mergedViews.collect() == [
                ([View.of("android.widget.TextView", "text1").build(),
                 View.of("android.widget.TextView", "text2").build()] as Set)
        ]
    }

    def "merges views with the same layout, android:id and id are different"() {
        mergedViews.add("test", [View.of("android.widget.TextView", "text").androidId().build()])
        mergedViews.add("test", [View.of("android.widget.TextView", "text").build()])

        expect:
        mergedViews.collect() == [
                ([View.of("android.widget.TextView", "text").androidId().build(),
                 View.of("android.widget.TextView", "text").build()] as Set)
        ]
    }
}
