Socket
======

## Why use Socket?

- Because you hate typeing findViewById() all the time.
- Because Butterknife requires non-zero boilerplate.
- Because ViewHolders are cool, but a pain to write.

## Usage
```groovy
buildscript {
    dependencies {
        classpath 'com.android.tools.build:gradle:0.12.+'
        classpath 'me.tatarka.socket:gradle-plugin:0.1'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'socket'
```

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
        android:padding="16dp"
        tools:text="Hello, Socket!"
        />
</LinearLayout>
```

### In an Activity

```java
public class ToolsActivity extends Activity {
    private SocketWrench socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket = new SocketWrench(findViewById(android.R.id.content));
        socket.text.setText("Hello, Socket!");
    }
}
```

### In a fragment

```java
public class ToolsFragment extends Fragment {
    private SocketWrench socket;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wrench, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        socket = new SocketWrench(view);
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
        SocketWrench socket;
        if (convertView == null) {
            socket = new SocketWrench(inflater.inflate(SocketWrench.LAYOUT, parent, false));
            socket.getView().setTag(socket);
        } else {
            socket = (SocketWrench) convertView.getTag();
        }
        socket.text.setText(getItem(position));
        return socket.getView();
    }
}
```
