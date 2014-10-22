package me.tatarka.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tatarka.sample.holdr.Holdr_FragmentListExample;
import me.tatarka.samplelibrary.TitledFragment;

/**
 * Created by evan on 8/24/14.
 */
public class ListExampleFragment extends TitledFragment {
    Holdr_FragmentListExample holdr;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(Holdr_FragmentListExample.LAYOUT, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentListExample(view);
        holdr.list.setAdapter(new MyListAdapter(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holdr = null;
    }

    @Override
    public String getTitle() {
        return "List Example";
    }

}
