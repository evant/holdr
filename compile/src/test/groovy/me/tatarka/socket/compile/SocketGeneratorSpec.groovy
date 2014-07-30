package me.tatarka.socket.compile

import spock.lang.Shared
import spock.lang.Specification

import static me.tatarka.socket.compile.SpecHelpers.code

class SocketGeneratorSpec extends Specification {
    @Shared
    SocketGenerator generator = new SocketGenerator("me.tatarka.test")

    def "an empty list of views generates an empty Socket"() {
        expect:
        code(generator, "test", []) == """
package me.tatarka.test.sockets;
$IMPORTS
public class SocketTest
    extends Socket
{

    public final static int LAYOUT = R.layout.test;

    public SocketTest(View view) {
        super(view);
    }

}
"""
    }

    def "a single view generates a Socket that instantiates that view"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").build()]) == """
package me.tatarka.test.sockets;
$IMPORTS
public class SocketTest
    extends Socket
{

    public final static int LAYOUT = R.layout.test;
    public android.widget.TextView myTextView;

    public SocketTest(View view) {
        super(view);
        myTextView = ((android.widget.TextView) view.findViewById(R.id.my_text_view));
    }

}
"""
    }

    def "a nested view generates a view holder with children for that view"() {
        expect:
        code(generator, "test", [View.of("android.widget.LinearLayout", "my_linear_layout")
                                         .child(View.of("android.widget.TextView", "my_text_view")).build()]) == """
package me.tatarka.test.sockets;
$IMPORTS
public class SocketTest
    extends Socket
{

    public final static int LAYOUT = R.layout.test;
    public android.widget.LinearLayout myLinearLayout;
    public android.widget.TextView myTextView;

    public SocketTest(View view) {
        super(view);
        myLinearLayout = ((android.widget.LinearLayout) view.findViewById(R.id.my_linear_layout));
        myTextView = ((android.widget.TextView) myLinearLayout.findViewById(R.id.my_text_view));
    }

}
"""
    }

    def "a view with a custom field name uses that name"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").fieldName("myCustomField").build()]) == """
package me.tatarka.test.sockets;
$IMPORTS
public class SocketTest
    extends Socket
{

    public final static int LAYOUT = R.layout.test;
    public android.widget.TextView myCustomField;

    public SocketTest(View view) {
        super(view);
        myCustomField = ((android.widget.TextView) view.findViewById(R.id.my_text_view));
    }

}
"""
    }

    def "a view with an android id uses android.R.id instead of R.id"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "text1").androidId().build()]) == """
package me.tatarka.test.sockets;

import android.view.View;
import me.tatarka.socket.Socket;

public class SocketTest
    extends Socket
{

    public final static int LAYOUT = me.tatarka.test.R.layout.test;
    public android.widget.TextView text1;

    public SocketTest(View view) {
        super(view);
        text1 = ((android.widget.TextView) view.findViewById(android.R.id.text1));
    }

}
"""
    }

    private static final String IMPORTS = """
import android.view.View;
import me.tatarka.socket.Socket;
import me.tatarka.test.R;
"""
}
