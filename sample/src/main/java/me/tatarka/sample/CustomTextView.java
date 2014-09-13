package me.tatarka.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by evan on 9/13/14.
 */
public class CustomTextView extends TextView {
    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
