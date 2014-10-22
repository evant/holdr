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
public class AdvancedExampleFragment extends TitledFragment {
    Holdr_FragmentAdvancedExample holdr;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(Holdr_FragmentAdvancedExample.LAYOUT, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentAdvancedExample(view);

        holdr.myHeader.setText("Hello, Holdr!");
        
        if (holdr.subHeaderPortrait != null) {
            holdr.subHeaderPortrait.setText("This is only in portrait.");
        }

        if (holdr.subHeaderLandscape != null) {
            holdr.subHeaderLandscape.setText("This is only in landscape.");
        }
        
        holdr.includeLayout.subHeader.setText("This is included.");
        
        holdr.differentView.setText("This is a different view in landscape and portrait");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holdr = null;
    }

    @Override
    public String getTitle() {
        return "Advanced Example";
    }
}
