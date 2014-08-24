package me.tatarka.socket.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tatarka.samplelibrary.TitledFragment;
import me.tatarka.socket.sample.sockets.Socket_FragmentAdvancedExample;

/**
 * Created by evan on 8/24/14.
 */
public class AdvancedExample extends TitledFragment {
    Socket_FragmentAdvancedExample socket;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advanced_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        socket = new Socket_FragmentAdvancedExample(view);
        
        socket.header.setText("Hello, Socket!");
        
        if (socket.subHeaderPortrait != null) {
            socket.subHeaderPortrait.setText("This is only in portrait.");
        } else {
            socket.subHeaderLandscape.setText("This is only in landscape.");
        }
        
        socket.includeLayout.subHeader.setText("This is included.");
    }

    @Override
    public String getTitle() {
        return "Advanced Example";
    }
}
