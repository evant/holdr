package me.tatarka.samplelibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tatarka.samplelibrary.sockets.Socket_FragmentLibraryExample;

/**
 * Created by evan on 8/24/14.
 */
public class LibraryExampleFragment extends TitledFragment {
    Socket_FragmentLibraryExample socket;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        socket = new Socket_FragmentLibraryExample(view);
        
        socket.header.setText("Hello, Socket!");
        socket.subHeader.setText("This is from a library.");
    }

    @Override
    public String getTitle() {
        return "Library Example";
    }
}
