package me.tatarka.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.tatarka.sample.holdr.Holdr_FragmentBasicExample2;
import me.tatarka.samplelibrary.TitledFragment;

/**
 * Created by evan on 8/24/14.
 */
public class BasicExampleFragment extends TitledFragment {
    Holdr_FragmentBasicExample2 holdr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(Holdr_FragmentBasicExample2.LAYOUT, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentBasicExample2(view);

        holdr.header.setText("Hello, Holdr!");
        holdr.subHeader.setText("This is so easy!");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holdr = null;
    }

    @Override
    public String getTitle() {
        return "Basic Example";
    }
}
