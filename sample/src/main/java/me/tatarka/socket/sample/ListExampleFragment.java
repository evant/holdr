package me.tatarka.socket.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import me.tatarka.samplelibrary.TitledFragment;
import me.tatarka.socket.sample.sockets.Socket_FragmentListExample;
import me.tatarka.socket.sample.sockets.Socket_ListItem;

/**
 * Created by evan on 8/24/14.
 */
public class ListExampleFragment extends TitledFragment {
    Socket_FragmentListExample socket;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        socket = new Socket_FragmentListExample(view);
        socket.list.setAdapter(new MyListAdapter());
    }

    @Override
    public String getTitle() {
        return "List Example";
    }
    
    private class MyListAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        
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
                socket = new Socket_ListItem(layoutInflater.inflate(R.layout.list_item, parent, false));
                socket.getView().setTag(socket);
            } else {
                socket = (Socket_ListItem) convertView.getTag();
            }
            
            socket.text.setText("Item " + getItem(position));
            
            return socket.getView();
        }
    }
}
