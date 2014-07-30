Socket
======

Because typing findViewById() in Android is such a pain.

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
<!-- activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>

<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:orientation="vertical" android:layout_width="match_parent"
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

```java
// MainActivity.java
public class MainActivity extends Activity {
    private SocketActivityMain socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket = new SocketActivityMain(findViewById(android.R.id.content));
        socket.text.setText("Hello, Socket!");
    }
}
```
