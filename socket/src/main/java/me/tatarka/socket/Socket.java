package me.tatarka.socket;

import android.view.View;

/**
 * A {@code Socket} is an object that holds references to specific views in a layout. Think of it
 * like a ViewHolder that is generated for you.
 */
public abstract class Socket {
    private final View view;

    /**
     * Constructs a new {@code Socket}.
     *
     * @param view The root view to find all the Socket views.
     */
    public Socket(View view) {
        this.view = view;
    }

    /**
     * The root view that the {@code Socket} was constructed with.
     * @return The root view.
     */
    public View getView() {
        return view;
    }
}
