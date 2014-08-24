package me.tatarka.socket.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import me.tatarka.socket.sample.sockets.Socket_CustomViewLayout;

/**
 * Created by evan on 8/24/14.
 */
public class CustomView extends LinearLayout {
    private Socket_CustomViewLayout socket;
    
    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        inflate(getContext(), R.layout.custom_view_layout, this);
        socket = new Socket_CustomViewLayout(this);
        
        socket.text.setText("This is in a custom view.");
    } 
}
