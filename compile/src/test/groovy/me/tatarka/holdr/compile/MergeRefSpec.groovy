package me.tatarka.holdr.compile

import spock.lang.Specification;

/**
 * Created by evan on 8/24/14.
 */
public class MergeRefSpec extends Specification {
    def "2 views with the same type merge to that type"() {
        when:
        View view1 = View.of("test", "id").build()
        View view2 = View.of("test", "id").build()
        View merged = Ref.merge("test", view1, view2)
        
        then:
        merged.id == "id"
        merged.type == "test"
    }
    
    def "2 views with different types merge to View"() {
        when:
        View view1 = View.of("test1", "id").build()
        View view2 = View.of("test2", "id").build()
        View merged = Ref.merge("test", view1, view2)

        then:
        merged.id == "id"
        merged.type == "android.view.View"
    }
    
    def "2 includes with the same layout merge"() {
        when:
        Include include1 = Include.of("test", "id").build()
        Include include2 = Include.of("test", "id").build()
        Include merged = Ref.merge("test", include1, include2)

        then:
        merged.id == "id"
        merged.layout == "test"
    }
    
    def "2 includes with different layouts throws an exception"() {
        when:
        Include include1 = Include.of("test1", "id").build()
        Include include2 = Include.of("test2", "id").build()
        Include merged = Ref.merge("test", include1, include2)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "a view and an include throws an exception"() {
        when:
        View view = View.of("test", "id").build()
        Include include = Include.of("test", "id").build()
        Include merged = Ref.merge("test", view, include)

        then:
        thrown(IllegalArgumentException)
    }
}
