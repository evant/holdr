package me.tatarka.holdr;

import android.view.View;

/**
 * A {@code Holdr} is an object that holds references to specific views in a layout. Think of it
 * like a ViewHolder that is generated for you.
 */
public abstract class Holdr {
    private final View view;

    /**
     * Constructs a new {@code Holdr}.
     *
     * @param view The root view to find all the holdr's views.
     */
    public Holdr(View view) {
        this.view = view;
    }

    /**
     * The root view that the {@code Holdr} was constructed with.
     * @return The root view.
     */
    public View getView() {
        return view;
    }
}
