package me.tatarka.holdr2;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tatarka.holdr2.internal.LayoutMapping;

public class LayoutUtil {

    private static final LayoutMapping mapping = new LayoutMapping();

    public static <L> L create(@LayoutRes int layoutRes, View view) {
        return (L) mapping.create(layoutRes, view);
    }

    public static <L> L setContentView(Activity activity, @LayoutRes int layoutRes) {
        ViewGroup contentParent = (ViewGroup) activity.findViewById(android.R.id.content);
        View view = activity.getLayoutInflater().inflate(layoutRes, contentParent, false);
        contentParent.addView(view);
        return create(layoutRes, view);
    }

    public static <L> L inflate(LayoutInflater inflater, @LayoutRes int layoutRes, @Nullable ViewGroup parent) {
        return inflate(inflater, layoutRes, parent, parent != null);
    }

    public static <L> L inflate(LayoutInflater inflater, @LayoutRes int layoutRes, @Nullable ViewGroup parent, boolean attachToRoot) {
        View view;
        if (parent != null && attachToRoot) {
            inflater.inflate(layoutRes, parent, true);
            view = parent.getChildAt(0);
        } else {
            view = inflater.inflate(layoutRes, parent, attachToRoot);
        }
        return create(layoutRes, view);
    }
}
