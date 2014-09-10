package me.tatarka.holdr.compile

import me.tatarka.holdr.compile.model.Include
import me.tatarka.holdr.compile.model.Listener
import me.tatarka.holdr.compile.model.View
import spock.lang.Shared
import spock.lang.Specification

import static SpecHelpers.code

class HoldrGeneratorSpec extends Specification {
    @Shared
    HoldrGenerator generator = new HoldrGenerator("me.tatarka.test")

    def "an empty list of views generates an empty Holdr"() {
        expect:
        generator.generate(Layout.of("test").build()) == """
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
        generator.generate(Layout.of("test")
                .view(View.of("android.widget.TextView", "my_text_view"))
                .build()) == """
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
        generator.generate(Layout.of("test")
                .view(View.of("android.widget.TextView", "my_text_view").fieldName("myCustomField"))
                .build()) == """
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
        generator.generate(Layout.of("test")
                .view(View.of("android.widget.TextView", "text1").androidId())
                .build()) == """
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
        generator.generate(Layout.of("test")
                .include(Include.of("my_layout", "my_include"))
                .build()) == """
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
        generator.generate(Layout.of("test")
                .view(View.of("android.widget.TextView", "my_text_view").nullable())
                .build()) == """
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

    def "a custom superclass generates a Holdr that subclasses that superclass"() {
        expect:
        generator.generate(Layout.of("test").superclass("test.TestHoldr").build()) == """
package me.tatarka.test.holdr;

import android.view.View;
import me.tatarka.test.R;
import test.TestHoldr;

public class Holdr_Test
    extends TestHoldr
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

    def "a view with a listener generates a Holdr that can accept that listener"() {
        expect:
        generator.generate(Layout.of("test")
                .view(View.of("android.widget.Button", "my_button")
                .listener(Listener.Type.ON_CLICK)
                .listener(Listener.Type.ON_LONG_CLICK)
                .listener(Listener.Type.ON_TOUCH))
                .build()) == """
package me.tatarka.test.holdr;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import me.tatarka.holdr.Holdr;
import me.tatarka.test.R;

public class Holdr_Test
    extends Holdr
{

    public final static int LAYOUT = R.layout.test;
    /**
     * View for {@link me.tatarka.test.R.id#my_button}.
     * 
     */
    public android.widget.Button myButton;
    private Holdr_Test.Listener _holdrListener;

    /**
     * Constructs a new {@link me.tatarka.holdr.Holdr} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the holdr's views.
     */
    public Holdr_Test(View view) {
        super(view);
        myButton = ((android.widget.Button) view.findViewById(R.id.my_button));
        myButton.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View view) {
                if (_holdrListener!= null) {
                    _holdrListener.onMyButtonClick(myButton);
                }
            }

        }
        );
        myButton.setOnLongClickListener(new OnLongClickListener() {


            @Override
            public boolean onLongClick(View view) {
                if (_holdrListener!= null) {
                    return _holdrListener.onMyButtonLongClick(myButton);
                }
                return false;
            }

        }
        );
        myButton.setOnTouchListener(new OnTouchListener() {


            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (_holdrListener!= null) {
                    return _holdrListener.onMyButtonTouch(myButton, event);
                }
                return false;
            }

        }
        );
    }

    public void setListener(Holdr_Test.Listener listener) {
        _holdrListener = listener;
    }

    public interface Listener {


        public void onMyButtonClick(android.widget.Button myButton);

        public boolean onMyButtonLongClick(android.widget.Button myButton);

        public boolean onMyButtonTouch(android.widget.Button myButton, MotionEvent event);

    }

}
"""
    }

    def "a nullable view with a listener generates a Holdr that can accept that listener but guards against null"() {
        expect:
        generator.generate(Layout.of("test")
                .view(View.of("android.widget.Button", "my_button")
                .listener(Listener.Type.ON_CLICK).nullable())
                .build()) == """
package me.tatarka.test.holdr;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import me.tatarka.holdr.Holdr;
import me.tatarka.test.R;

public class Holdr_Test
    extends Holdr
{

    public final static int LAYOUT = R.layout.test;
    /**
     * View for {@link me.tatarka.test.R.id#my_button}.
     * 
     */
    @Nullable
    public android.widget.Button myButton;
    private Holdr_Test.Listener _holdrListener;

    /**
     * Constructs a new {@link me.tatarka.holdr.Holdr} for {@link me.tatarka.test.R.layout#test}.
     * 
     * @param view
     *     The root view to search for the holdr's views.
     */
    public Holdr_Test(View view) {
        super(view);
        myButton = ((android.widget.Button) view.findViewById(R.id.my_button));
        if (myButton!= null) {
            myButton.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    if (_holdrListener!= null) {
                        _holdrListener.onMyButtonClick(myButton);
                    }
                }

            }
            );
        }
    }

    public void setListener(Holdr_Test.Listener listener) {
        _holdrListener = listener;
    }

    public interface Listener {


        public void onMyButtonClick(android.widget.Button myButton);

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
