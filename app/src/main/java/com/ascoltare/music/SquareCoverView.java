package com.ascoltare.music;

import android.content.Context;
import android.view.View;

public class SquareCoverView extends CoverView {
    public SquareCoverView(Context context, String type) {
        super(context, type);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        setMeasuredDimension(width, width);
    }
}
