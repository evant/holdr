package me.tatarka.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import me.tatarka.sample.holdr.Holdr_ListItem;

/**
* Created by evan on 8/31/14.
*/
class MyListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    
    public MyListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }
    
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
