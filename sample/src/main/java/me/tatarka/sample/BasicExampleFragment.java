package me.tatarka.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import me.tatarka.sample.holdr.Holdr_FragmentBasicExample;
import me.tatarka.samplelibrary.TitledFragment;

/**
 * Created by evan on 8/24/14.
 */
public class BasicExampleFragment extends TitledFragment {
    Holdr_FragmentBasicExample holdr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_basic_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentBasicExample(view);
        
        holdr.header.setText("Hello, Holdr!");
        holdr.subHeader.setText("This is so easy!");
        
        holdr.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "You Clicked Me!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public String getTitle() {
        return "Basic Example";
    }
}
