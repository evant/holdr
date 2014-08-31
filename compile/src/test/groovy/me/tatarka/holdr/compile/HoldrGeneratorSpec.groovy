package me.tatarka.holdr.compile

import spock.lang.Shared
import spock.lang.Specification

import static SpecHelpers.code

class HoldrGeneratorSpec extends Specification {
    @Shared
    HoldrGenerator generator = new HoldrGenerator("me.tatarka.test")

    def "an empty list of views generates an empty Holdr"() {
        expect:
        code(generator, "test", [] as Set) == """
package me.tatarka.test.holdr;
$IMPORTS
public class Holdr_Test
    extends Holdr
{

    public final static int LAYOUT = R.layout.test;

    /**
     * Constructs a new {@link me.tatarka.holdr.Holdr} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the holdr's views.
     */
    public Holdr_Test(View view) {
        super(view);
    }

}
"""
    }

    def "a single view generates a Holdr that instantiates that view"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").build()] as Set) == """
package me.tatarka.test.holdr;
$IMPORTS
public class Holdr_Test
    extends Holdr
{

    public final static int LAYOUT = R.layout.test;
    /**
     * View for {@link me.tatarka.test.R.id#my_text_view}.
     * 
     */
    public android.widget.TextView myTextView;

    /**
     * Constructs a new {@link me.tatarka.holdr.Holdr} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the holdr's views.
     */
    public Holdr_Test(View view) {
        super(view);
        myTextView = ((android.widget.TextView) view.findViewById(R.id.my_text_view));
    }

}
"""
    }

    def "a view with a custom field name uses that name"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").fieldName("myCustomField").build()] as Set) == """
package me.tatarka.test.holdr;
$IMPORTS
public class Holdr_Test
    extends Holdr
{

    public final static int LAYOUT = R.layout.test;
    /**
     * View for {@link me.tatarka.test.R.id#my_text_view}.
     * 
     */
    public android.widget.TextView myCustomField;

    /**
     * Constructs a new {@link me.tatarka.holdr.Holdr} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the holdr's views.
     */
    public Holdr_Test(View view) {
        super(view);
        myCustomField = ((android.widget.TextView) view.findViewById(R.id.my_text_view));
    }

}
"""
    }

    def "a view with an android id uses android.R.id instead of R.id"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "text1").androidId().build()] as Set) == """
package me.tatarka.test.holdr;

import android.view.View;
import me.tatarka.holdr.Holdr;

public class Holdr_Test
    extends Holdr
{

    public final static int LAYOUT = me.tatarka.test.R.layout.test;
    /**
     * View for {@link android.R.id#text1}.
     * 
     */
    public android.widget.TextView text1;

    /**
     * Constructs a new {@link me.tatarka.holdr.Holdr} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the holdr's views.
     */
    public Holdr_Test(View view) {
        super(view);
        text1 = ((android.widget.TextView) view.findViewById(android.R.id.text1));
    }

}
"""
    }

    def "an include with an id generates a reference to a holdr with it's layout"() {
        expect:
        code(generator, "test", [Include.of("my_layout", "my_include").build()] as Set) == """
package me.tatarka.test.holdr;
$IMPORTS
public class Holdr_Test
    extends Holdr
{

    public final static int LAYOUT = R.layout.test;
    /**
     * Holdr for {@link me.tatarka.test.R.layout#my_layout}.
     * 
     */
    public Holdr_MyLayout myInclude;

    /**
     * Constructs a new {@link me.tatarka.holdr.Holdr} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the holdr's views.
     */
    public Holdr_Test(View view) {
        super(view);
        myInclude = new Holdr_MyLayout(view);
    }

}
"""
    }
    
    def "a single nullable view generates a Holdr that instantiates that view with an annotation"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").nullable().build()] as Set) == """
package me.tatarka.test.holdr;

import android.support.annotation.Nullable;
import android.view.View;
import me.tatarka.holdr.Holdr;
import me.tatarka.test.R;

public class Holdr_Test
    extends Holdr
{

    public final static int LAYOUT = R.layout.test;
    /**
     * View for {@link me.tatarka.test.R.id#my_text_view}.
     * 
     */
    @Nullable
    public android.widget.TextView myTextView;

    /**
     * Constructs a new {@link me.tatarka.holdr.Holdr} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the holdr's views.
     */
    public Holdr_Test(View view) {
        super(view);
        myTextView = ((android.widget.TextView) view.findViewById(R.id.my_text_view));
    }

}
"""
    }

    private static final String IMPORTS = """
import android.view.View;
import me.tatarka.holdr.Holdr;
import me.tatarka.test.R;
"""
}
