package me.tatarka.socket.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import me.tatarka.socket.sample.sockets.Socket_ActivityMain;

public class MainActivity extends ActionBarActivity {
    private Socket_ActivityMain socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket = new Socket_ActivityMain(findViewById(android.R.id.content));
        
    }
}
