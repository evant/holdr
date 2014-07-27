package me.tatarka.socket;

import android.view.View;

public abstract class Socket {
    private final View view;

    public Socket(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }
}
