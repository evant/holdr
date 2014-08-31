package me.tatarka.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import me.tatarka.sample.holdr.Holdr_FragmentCallbackExample;
import me.tatarka.samplelibrary.TitledFragment;

/**
 * Created by evan on 8/31/14.
 */
public class CallbackExampleFragment extends TitledFragment implements Holdr_FragmentCallbackExample.Listener {
    private Holdr_FragmentCallbackExample holdr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_callback_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentCallbackExample(view);
        holdr.setListener(this);
    }

    @Override
    public String getTitle() {
        return "Callback Example";
    }

    @Override
    public void onClickButtonClick(Button clickButton) {
        Toast.makeText(getActivity(), "You clicked me!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClickButtonLongClick(Button longClickButton) {
        Toast.makeText(getActivity(), "You long clicked me!", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onTouchButtonTouch(Button touchButton, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            Toast.makeText(getActivity(), "You touched me (down)!", Toast.LENGTH_SHORT).show();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Toast.makeText(getActivity(), "You touched me (up)!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onCheckboxCheckedChanged(CheckBox checkbox, boolean isChecked) {
        Toast.makeText(getActivity(), "You checked me! (" + isChecked + ")", Toast.LENGTH_SHORT).show();
    }
}
