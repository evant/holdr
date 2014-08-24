package me.tatarka.socket.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import me.tatarka.samplelibrary.TitledFragment;
import me.tatarka.socket.sample.sockets.Socket_FragmentBasicExample;

/**
 * Created by evan on 8/24/14.
 */
public class BasicExampleFragment extends TitledFragment {
    Socket_FragmentBasicExample socket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_basic_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        socket = new Socket_FragmentBasicExample(view);
        
        socket.header.setText("Hello, Socket!");
        socket.subHeader.setText("This is so easy!");
        
        socket.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "You Clicked Me!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public String getTitle() {
        return "Basic Example";
    }
}
