package me.tatarka.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tatarka.sample.holdr.Holdr_FragmentAdvancedExample;
import me.tatarka.samplelibrary.TitledFragment;

/**
 * Created by evan on 8/24/14.
 */
public class AdvancedExample extends TitledFragment {
    Holdr_FragmentAdvancedExample holdr;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advanced_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentAdvancedExample(view);
        
        holdr.header.setText("Hello, Holdr!");
        
        if (holdr.subHeaderPortrait != null) {
            holdr.subHeaderPortrait.setText("This is only in portrait.");
        } else {
            holdr.subHeaderLandscape.setText("This is only in landscape.");
        }
        
        holdr.includeLayout.subHeader.setText("This is included.");
    }

    @Override
    public String getTitle() {
        return "Advanced Example";
    }
}
