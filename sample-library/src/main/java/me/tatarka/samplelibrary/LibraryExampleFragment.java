package me.tatarka.samplelibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tatarka.samplelibrary.holdr.Holdr_FragmentLibraryExample;

/**
 * Created by evan on 8/24/14.
 */
public class LibraryExampleFragment extends TitledFragment {
    Holdr_FragmentLibraryExample holdr;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentLibraryExample(view);
        
        holdr.header.setText("Hello, Holdr!");
        holdr.subHeader.setText("This is from a library.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holdr = null;
    }

    @Override
    public String getTitle() {
        return "Library Example";
    }
}
