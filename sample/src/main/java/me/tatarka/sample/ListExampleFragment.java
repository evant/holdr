package me.tatarka.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import me.tatarka.sample.holdr.Holdr_FragmentListExample;
import me.tatarka.sample.holdr.Holdr_ListItem;
import me.tatarka.samplelibrary.TitledFragment;

/**
 * Created by evan on 8/24/14.
 */
public class ListExampleFragment extends TitledFragment {
    Holdr_FragmentListExample holdr;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentListExample(view);
        holdr.list.setAdapter(new MyListAdapter());
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
            Holdr_ListItem holdr;
            if (convertView == null) {
                holdr = new Holdr_ListItem(layoutInflater.inflate(R.layout.list_item, parent, false));
                holdr.getView().setTag(holdr);
            } else {
                holdr = (Holdr_ListItem) convertView.getTag();
            }
            
            holdr.text.setText("Item " + getItem(position));
            
            return holdr.getView();
        }
    }
}
