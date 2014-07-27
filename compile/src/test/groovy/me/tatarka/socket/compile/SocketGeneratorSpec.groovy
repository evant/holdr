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
public class TestSocket
    extends Socket
{

    public final static int LAYOUT = me.tatarka.test.R.layout.test;

    private TestSocket(View view) {
        super(view);
    }
$STATIC_METHODS
}
"""
    }

    def "a single view generates a Socket that instantiates that view"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").build()]) == """
package me.tatarka.test.sockets;
$IMPORTS
public class TestSocket
    extends Socket
{

    public final static int LAYOUT = me.tatarka.test.R.layout.test;
    public android.widget.TextView myTextView;

    private TestSocket(View view) {
        super(view);
        myTextView = ((android.widget.TextView) view.findViewById(me.tatarka.test.R.id.my_text_view));
    }
$STATIC_METHODS
}
"""
    }

    def "a nested view generates a view holder with children for that view"() {
        expect:
        code(generator, "test", [View.of("android.widget.LinearLayout", "my_linear_layout")
                                         .child(View.of("android.widget.TextView", "my_text_view")).build()]) == """
package me.tatarka.test.sockets;
$IMPORTS
public class TestSocket
    extends Socket
{

    public final static int LAYOUT = me.tatarka.test.R.layout.test;
    public android.widget.LinearLayout myLinearLayout;
    public android.widget.TextView myTextView;

    private TestSocket(View view) {
        super(view);
        myLinearLayout = ((android.widget.LinearLayout) view.findViewById(me.tatarka.test.R.id.my_linear_layout));
        myTextView = ((android.widget.TextView) myLinearLayout.findViewById(me.tatarka.test.R.id.my_text_view));
    }
$STATIC_METHODS
}
"""
    }

    def "a view with a custom field name uses that name"() {
        expect:
        code(generator, "test", [View.of("android.widget.TextView", "my_text_view").fieldName("myCustomField").build()]) == """
package me.tatarka.test.sockets;
$IMPORTS
public class TestSocket
    extends Socket
{

    public final static int LAYOUT = me.tatarka.test.R.layout.test;
    public android.widget.TextView myCustomField;

    private TestSocket(View view) {
        super(view);
        myCustomField = ((android.widget.TextView) view.findViewById(me.tatarka.test.R.id.my_text_view));
    }
$STATIC_METHODS
}
"""
    }

    private static final String IMPORTS = """
import android.app.Activity;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.tatarka.socket.Socket;
"""

    private static final String STATIC_METHODS = """
    public static TestSocket from(View view) {
        return new TestSocket(view);
    }

    public static TestSocket from(Activity activity) {
        return new TestSocket(activity.findViewById(android.R.id.content));
    }

    public static TestSocket from(Fragment fragment) {
        return new TestSocket(fragment.getView());
    }

    public static TestSocket inflate(LayoutInflater layoutInflater, int resource, ViewGroup root, boolean attachToRoot) {
        return new TestSocket(layoutInflater.inflate(resource, root, attachToRoot));
    }

    public static TestSocket inflate(LayoutInflater layoutInflater, int resource, ViewGroup root) {
        return new TestSocket(layoutInflater.inflate(resource, root));
    }

    public static TestSocket listInflate(LayoutInflater layoutInflater, View convertView, ViewGroup parent) {
        if (convertView == null) {
            View view = layoutInflater.inflate(LAYOUT, parent, false);
            TestSocket socket = new TestSocket(view);
            view.setTag(socket);
            return socket;
        } else {
            return ((TestSocket) convertView.getTag());
        }
    }
"""
}
