Holdr
======

## Why use Holdr?

- Because you hate typing `findViewById()` all the time.
- Because Butterknife requires non-zero boilerplate and doesn't work well in
    library projects.
- Because view holders are cool, but a pain to write.

## Usage

Simply apply the gradle plugin and your done!

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.12.2'
        classpath 'me.tatarka.holdr:gradle-plugin:1.0.1'
    }
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
        tools:text="Hello, Holdr!"
        />
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
        holdr = new Holdr_Wrench(findViewById(android.R.id.content));
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
        holdr.text.setText("Hello, Holdr!");;
    }
}
```

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
        app:holdr_include="view"
        />
`   
    <TextView
        android:id="@+id/text2"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        tools:text="Hello, Holdr!"
        />
</LinearLayout>
```

would include only `text1` in the generated class.

Note: The current implementation only allows you to nest these attributes 2
levels deep (ignore inside include inside ignore won't work). I don't think
there is a use case complex enough to warrant this, but it may be fixed in a
later version if there is a need.

Finally, if you don't like the field name generated for a specific id, you can
set it yourself by using `holdr_field_name="myBetterFieldName"` on a view.
