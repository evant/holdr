package me.tatarka.socket.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import me.tatarka.socket.sample.sockets.SocketActivityMain;
import me.tatarka.socket.sample.sockets.SocketListItem;

public class MainActivity extends Activity {
    private SocketActivityMain socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket = new SocketActivityMain(findViewById(android.R.id.content));
        socket.text.setText("Hello, Socket!");
        
        if (socket.textLand != null) {
            socket.textLand.setText("Hello, Socket Land!");
        }
        
        if (socket.includeLayout.text1 != null) {
            socket.includeLayout.text1.setText("Hello, Include Text 1");
            socket.includeLayout.text2.setText("Hello, Include Text 2");
        }
        
        if (socket.different instanceof Button) {
            socket.different.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Pressed a button!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        socket.list.setAdapter(new MyAdapter());
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 50;
        }

        @Override
        public Integer getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SocketListItem socket;
            if (convertView == null) {
                socket = new SocketListItem(getLayoutInflater().inflate(SocketListItem.LAYOUT, parent, false));
                socket.getView().setTag(socket);
            } else {
                socket = (SocketListItem) convertView.getTag();
            }
            socket.text.setText("Item " + getItem(position));
            return socket.getView();
        }
    }
}
