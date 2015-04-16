package com.bruyu.imagewalker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * FrameLayout sub class which implements Checkable.
 * it is used for GridView in multi-choice mode
 */
public class CheckableLayout extends FrameLayout implements Checkable {
    private boolean mChecked;

    public CheckableLayout(Context context){
        super(context);
    }

    @Override
    public void setChecked(boolean checked){
        mChecked = checked;
        Drawable drawable
                = getResources().getDrawable(R.drawable.allblack);
        drawable.setAlpha(150);
        this.setForeground(checked ? drawable : null);
    }

    @Override
    public boolean isChecked(){
        return mChecked;
    }

    @Override
    public void toggle(){
        mChecked = !mChecked;
    }
}
