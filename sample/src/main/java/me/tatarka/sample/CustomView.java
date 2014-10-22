package me.tatarka.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import me.tatarka.sample.holdr.Holdr_CustomViewLayout;


/**
 * Created by evan on 8/24/14.
 */
public class CustomView extends LinearLayout {
    private Holdr_CustomViewLayout holdr;
    
    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        inflate(getContext(), Holdr_CustomViewLayout.LAYOUT, this);
        holdr = new Holdr_CustomViewLayout(this);
        
        holdr.text.setText("This is in a custom view.");
    } 
}
