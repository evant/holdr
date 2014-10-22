package me.tatarka.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
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
        return inflater.inflate(Holdr_FragmentCallbackExample.LAYOUT, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holdr = new Holdr_FragmentCallbackExample(view);
        holdr.setListener(this);
        holdr.list.setAdapter(new MyListAdapter(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holdr = null;
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
    public boolean onTouchButtonTouch(Button touchButton, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Toast.makeText(getActivity(), "You touched me (down)!", Toast.LENGTH_SHORT).show();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Toast.makeText(getActivity(), "You touched me (up)!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onCheckboxCheckedChanged(CheckBox checkbox, boolean isChecked) {
        Toast.makeText(getActivity(), "You checked me! (" + isChecked + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFocusTextFocusChange(TextView focusText, boolean hasFocus) {
        Toast.makeText(getActivity(), "You focused me! (" + hasFocus + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShareButtonClick(Button view) {
        Toast.makeText(getActivity(), "You clicked shared button! (" + (view.getId() == R.id.share_button1 ? "1" : "2") + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListItemClick(ListView list, View item, int position, long id) {
        Toast.makeText(getActivity(), "You item clicked me! (" + position + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onListItemLongClick(ListView list, View item, int position, long id) {
        Toast.makeText(getActivity(), "You item long clicked me! (" + position + ")", Toast.LENGTH_SHORT).show();
        return false;
    }
}
