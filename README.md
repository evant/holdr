Holdr
======

## What is Holdr?

Holdr generates classes based on your layouts to help you interact with them in
a type-safe way. It removes the boilerplate of doing 
`TextView myTextView = findViewById(R.id.my_text_view)` all the time.

## Doesn't [Butter Knife](http://jakewharton.github.io/butterknife/)/[AndroidAnnotaions](http://androidannotations.org/)/[RoboGuice](https://github.com/roboguice/roboguice) already do that?

This is a different approach to solving the same problem, the important
difference is your layout dictates what is generated instead of annotations on
your classes. This means that it's much less likely for your code and layouts to
get out of sync.

This approach also means zero reflection (and no proguard issues) and works 
equally as well in library projects.

## Usage

Simply apply the gradle plugin and your done!

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.12.2'
        classpath 'me.tatarka.holdr:gradle-plugin:1.3.0'
    }
}

repositories {
    mavenCentral()
}

apply plugin: 'com.android.application'
apply plugin: 'me.tatarka.holdr'
```

Say you have a layout file `hand.xml`.

```xml
<!-- hand.xml -->
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
        tools:text="Hello, Holdr!"/>
</LinearLayout>
```

Holdr will create a class for you named `your.application.id.holdr.Holdr_Hand`.
This class is basically a view holder that you can instantiate anywhere you have
a view.

### In an Activity

```java
public class MyActivity extends Activity {
    private Holdr_Hand holdr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hand);
        holdr = new Holdr_Hand(findViewById(android.R.id.content));
        holdr.text.setText("Hello, Holdr!");
    }
}
```

### In a Fragment

```java
public class MyFragment extends Fragment {
    private Holdr_Hand holdr;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hand, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_Hand(view);
        holdr.text.setText("Hello, Holdr!");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holdr = null;
    }
}
```

### In an Adapter

```java
public class MyAdapter extends BaseAdapter {
    // other methods
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holdr_Hand holdr;
        if (convertView == null) {
            holdr = new Holdr_Hand(inflater.inflate(R.layout.hand, parent, false));
            holdr.getView().setTag(holdr);
        } else {
            holdr = (Holdr_Hand) convertView.getTag();
        }
        holdr.text.setText(getItem(position));
        return holdr.getView();
    }
}
```

### In a Custom View

```java
public class MyCustomView extends LinearLayout {
    Holdr_Hand holdr;
    
    // other methods
    
    private void init() {
        holdr = new Holdr_Hand(inflate(getContext(), R.layout.hand, this));
        holdr.text.setText("Hello, Holdr!");
    }
}
```

### Multiple layouts

You may have multiple instances of a layout (in `layout` and `layout-land` for
example). In that case Holdr will merge the id's accross them. If an id appears
in one and not the other, a `@Nullable` annotation will be generated to warn you
of this.

If the type of the view doesn't match, Holdr will take the most
conservative route and use type `View`. If instead, they share a common
superclass and you want to use that, you can use the `app:holdr_class` to
override the view type so that they match.

```xml
<!-- layout/hand.xml -->
<TextView
    android:id="@+id/text"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    tools:text="Hello, Holdr!"/>

<!-- layout-land/hand.xml -->
<com.example.MyCustomTextView
    android:id="@+id/text"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    tools:text="Hello, Holdr!"
    app:holdr_class="TextView"/>
```

### Callback Listeners

You can also specify listeners for your Activity/Fragment/Whatever to handle
to make working with callbacks a bit nicer. For example, if you had the layout
file `hand.xml`,

  ```xml
<!-- hand.xml -->
<?xml version="1.0" encoding="utf-8"?>

<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:id="@+id/my_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Hello, Holdr!"
        app:holdr_onClick="true"/>
</LinearLayout>
  ```

The generated `Holdr_Hand` class will also have a listener interface for you to
implement.

  ```java
public class MyActivity extends Activity implements Holdr_Hand.Listener {
    private Holdr_Hand holdr;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hand);
        holdr = new Holdr_Hand(findViewById(android.R.id.content));
        holdr.setListener(this);
    }
    
    @Override
    public void onMyButtonClick(Button myButton) {
        // Handle button click.
    }
}
```

Here is a list of all the listeners you can handle:
- `holdr_onTouch`
- `holdr_onClick`
- `holdr_onLongClick`
- `holdr_onFocusChange`
- `holdr_onCheckedChanged`
- `holdr_onEditorAction`
- `holdr_onItemClick`
- `holdr_onItemLongClick`

You can also specify a custom method name by doing
`app:holdr_onClick="myCustomMethodName"` instead. You can also specify the same
name on multiple views and they will share a listener (provided the listeners
are of the same type).

### Custom Superclass

Want to use a `Holdr` in a place where you need a specific subclass?
(`RecyclerView.ViewHolder` for example).  Just use the attribute
`app:holdr_superclass="com.example.MySuperclass` and it will subclass that
instead of `Holdr`. The only requirement is that the superclass must contain a
constructor that takes a `View`.

### Controlling What's Generated

If you don't like the idea of a whole bunch of code being generated for all your
layouts (It's really not much, I promise!), you can add `holdr.defaultInclude
false` to your `build.gradle` and then you can manually opt-in for each of your
layouts.

The easiest way to opt-in is to add `app:holdr_include="all"` to the root
view of that layout.

By default, every view with an id gets added to the generated class. You can use
the attributes `holdr_include` and `holdr_ignore` to get more granular
control. Both take either the value `"view"` to act on just the view it's used
on or `"all"` to act on that view and all it's children. For example,

   ```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:holdr_ignore="all">

    <TextView
        android:id="@+id/text1"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        tools:text="Hello, Holdr!"
        app:holdr_include="view"/>
    `   
    <TextView
        android:id="@+id/text2"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        tools:text="Hello, Holdr!"/>
</LinearLayout>
   ```

would include only `text1` in the generated class.

Note: The current implementation only allows you to nest these attributes 2
levels deep (ignore inside include inside ignore won't work). I don't think
there is a use case complex enough to warrant this, but it may be fixed in a
later version if there is a need.

Finally, if you don't like the field name generated for a specific id, you can
set it yourself by using `app:holdr_field_name="myBetterFieldName"` on a view.

### Android Studio Plugin

Tired of having to build your project after every layout change? With the intellij plugin the Holdr classes will be auto-generated as soon as you save!

Go to `Settings -> Plugins -> Browse Repositories...` and search for "Holdr".

If instead you feel like living on the edge, you can download the [zip](https://github.com/evant/holdr/blob/master/intellij-plugin/intellij-plugin.zip?raw=true) and go to `Settings -> Plugins -> Install plugin from disk...` to install.

(Requires Android Studio `0.6.0+` or Intellij 14)
