package me.tatarka.socket.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import me.tatarka.socket.sample.sockets.ActivityMainSocket;
import me.tatarka.socket.sample.sockets.ListItemSocket;


public class MainActivity extends Activity {
    private ActivityMainSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket = new ActivityMainSocket(findViewById(android.R.id.content));
        socket.text.setText("Hello, Socket!");
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
            ListItemSocket socket;
            if (convertView == null) {
                socket = new ListItemSocket(getLayoutInflater().inflate(ListItemSocket.LAYOUT, parent, false));
                socket.getView().setTag(socket);
            } else {
                socket = (ListItemSocket) convertView.getTag();
            }
            socket.text.setText("Item " + getItem(position));
            return socket.getView();
        }
    }
}
