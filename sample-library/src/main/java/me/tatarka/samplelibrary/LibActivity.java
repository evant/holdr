package me.tatarka.samplelibrary;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import me.tatarka.samplelibrary.sockets.Socket_LibLayout;
import me.tatarka.samplelibrary.sockets.Socket_ListItem;

/**
 * Created by evan on 8/24/14.
 */
public class LibActivity extends Activity {
    Socket_LibLayout socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_layout);
        socket = new Socket_LibLayout(findViewById(android.R.id.content));
        socket.text.setText("Socket in a library project!");
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
            Socket_ListItem socket;
            if (convertView == null) {
                socket = new Socket_ListItem(getLayoutInflater().inflate(Socket_ListItem.LAYOUT, parent, false));
                socket.getView().setTag(socket);
            } else {
                socket = (Socket_ListItem) convertView.getTag();
            }
            socket.text.setText("Item " + getItem(position));
            return socket.getView();
        }
    }
}
