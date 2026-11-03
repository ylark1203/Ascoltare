package com.ascoltare.music;

import android.content.Context;
import android.widget.FrameLayout;

public class SquareFrameLayout extends FrameLayout {
    public SquareFrameLayout(Context context) { super(context); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
