Socket
======

## Why use Socket?

- Because you hate typing `findViewById()` all the time.
- Because Butterknife requires non-zero boilerplate and doesn't work well in library projects.
- Because view holders are cool, but a pain to write.

## Usage

Simply apply the gradle plugin and your done!

```groovy
buildscript {
    dependencies {
        classpath 'com.android.tools.build:gradle:0.12.2'
        classpath 'me.tatarka.socket:gradle-plugin:0.1'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'socket'
```

Say you have a layout file `wrench.xml`.

```xml
<!-- wrench.xml -->
<?xml version="1.0" encoding="utf-8"?>

<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        tools:text="Hello, Socket!"
        />
</LinearLayout>
```

Socket will create a class for you named `your.application.id.socket.Socket_Wrench`. This class is basically a view holder that you can instantiate anywhere you have a view.

### In an Activity

```java
public class ToolsActivity extends Activity {
    private Socket_Wrench socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket = new Socket_Wrench(findViewById(android.R.id.content));
        socket.text.setText("Hello, Socket!");
    }
}
```

### In a Fragment

```java
public class ToolsFragment extends Fragment {
    private Socket_Wrench socket;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wrench, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        socket = new Socket_Wrench(view);
        socket.text.setText("Hello, Socket!");
    }
}
```

### In an Adapter

```java
public class ToolsAdapter extends BaseAdapter {
    // other methods

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Socket_Wrench socket;
        if (convertView == null) {
            socket = new Socket_Wrench(inflater.inflate(Socket_Wrench.LAYOUT, parent, false));
            socket.getView().setTag(socket);
        } else {
            socket = (Socket_Wrench) convertView.getTag();
        }
        socket.text.setText(getItem(position));
        return socket.getView();
    }
}
```

### Controlling What's Generated

If you don't like the idea of a whole bunch of code being generated for all your layouts (It's really not much, I promise!), you can add `socket.defaultInclude false` to your `build.gradle` and then you can manually opt-in for each of your layouts.

The easiest way to opt-in is to add `app:socket_include="children"` to the root view of that layout.

By default, every view with an id gets added to the generated class. You can use the attributes `socket_include` and `socket_ignore` to get more granular control. Both take either the value `"view"` to act on just the view it's used on or `"children"` to act on that view and all it's children. For example,

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:socket_ignore="children">

    <TextView
        android:id="@+id/text1"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        tools:text="Hello, Socket!"
        app:socket_include="view"
        />
`   
    <TextView
        android:id="@+id/text2"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        tools:text="Hello, Socket!"
        />
</LinearLayout>
```

would include only `text1` in the generated class.

Note: The current implementation only allows you to nest these attributes 2 levels deep (ignore inside include inside ignore won't work). I don't think there is a use case complex enough to warrant this, but it may be fixed in a later version if there is a need.

Finally, if you don't like the field name generated for a specific id, you can set it yourself by using `socket_field_name="myBetterFieldName"` on a view.
