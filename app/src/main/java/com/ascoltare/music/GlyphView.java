package com.ascoltare.music;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class GlyphView extends View {
    public static final int HOME = 0, SEARCH = 1, LIBRARY = 2, PLAY = 3, PAUSE = 4,
            HEART = 5, DEVICES = 6, MORE = 7, PLUS = 8, DOWN = 9, BACK = 10,
            FORWARD = 11, SHUFFLE = 12, REPEAT = 13, CHEVRON = 14, EDIT = 15;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private int icon;

    public GlyphView(Context context, int icon, int color) {
        super(context);
        this.icon = icon;
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setIcon(int icon) { this.icon = icon; invalidate(); }
    public void setColor(int color) { paint.setColor(color); invalidate(); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(getWidth() / 24f, getHeight() / 24f);
        paint.setStrokeWidth(1.7f);
        paint.setStyle(Paint.Style.STROKE);
        path.reset();
        switch (icon) {
            case HOME:
                path.moveTo(3, 10); path.lineTo(12, 3); path.lineTo(21, 10); path.lineTo(21, 21); path.lineTo(15, 21); path.lineTo(15, 14); path.lineTo(9, 14); path.lineTo(9, 21); path.lineTo(3, 21); path.close(); break;
            case SEARCH:
                canvas.drawCircle(10.5f, 10.5f, 7, paint); path.moveTo(16, 16); path.lineTo(21, 21); break;
            case LIBRARY:
                path.moveTo(4, 4); path.lineTo(4, 20); path.moveTo(9, 4); path.lineTo(9, 20); path.moveTo(14, 5); path.lineTo(19, 4); path.lineTo(22, 19); path.lineTo(17, 20); path.close(); break;
            case PLAY:
                paint.setStyle(Paint.Style.FILL); path.moveTo(7, 4); path.lineTo(20, 12); path.lineTo(7, 20); path.close(); break;
            case PAUSE:
                paint.setStrokeWidth(3.2f); path.moveTo(8, 5); path.lineTo(8, 19); path.moveTo(16, 5); path.lineTo(16, 19); break;
            case HEART:
                path.moveTo(12, 20); path.cubicTo(10, 18, 3, 13, 3, 8); path.cubicTo(3, 3, 9, 2, 12, 6); path.cubicTo(15, 2, 21, 3, 21, 8); path.cubicTo(21, 13, 14, 18, 12, 20); path.close(); break;
            case DEVICES:
                canvas.drawRect(3, 5, 21, 17, paint); path.moveTo(8, 21); path.lineTo(16, 21); path.moveTo(12, 17); path.lineTo(12, 21); break;
            case MORE:
                paint.setStyle(Paint.Style.FILL); canvas.drawCircle(5, 12, 1.3f, paint); canvas.drawCircle(12, 12, 1.3f, paint); canvas.drawCircle(19, 12, 1.3f, paint); break;
            case PLUS:
                path.moveTo(12, 5); path.lineTo(12, 19); path.moveTo(5, 12); path.lineTo(19, 12); break;
            case DOWN:
                path.moveTo(6, 9); path.lineTo(12, 15); path.lineTo(18, 9); break;
            case BACK:
                path.moveTo(5, 5); path.lineTo(5, 19); paint.setStyle(Paint.Style.FILL); path.moveTo(19, 6); path.lineTo(9, 12); path.lineTo(19, 18); path.close(); break;
            case FORWARD:
                path.moveTo(19, 5); path.lineTo(19, 19); paint.setStyle(Paint.Style.FILL); path.moveTo(5, 6); path.lineTo(15, 12); path.lineTo(5, 18); path.close(); break;
            case SHUFFLE:
                path.moveTo(16, 3); path.lineTo(21, 3); path.lineTo(21, 8); path.moveTo(4, 20); path.lineTo(21, 3); path.moveTo(4, 4); path.lineTo(9, 9); path.moveTo(15, 15); path.lineTo(21, 21); path.moveTo(16, 21); path.lineTo(21, 21); path.lineTo(21, 16); break;
            case REPEAT:
                path.moveTo(17, 2); path.lineTo(21, 5); path.lineTo(17, 8); path.moveTo(3, 11); path.lineTo(3, 9); path.cubicTo(3, 4, 7, 5, 21, 5); path.moveTo(7, 22); path.lineTo(3, 19); path.lineTo(7, 16); path.moveTo(21, 13); path.lineTo(21, 15); path.cubicTo(21, 20, 17, 19, 3, 19); break;
            case CHEVRON:
                path.moveTo(9, 5); path.lineTo(16, 12); path.lineTo(9, 19); break;
            case EDIT:
                path.moveTo(4, 20); path.lineTo(5, 16); path.lineTo(15, 6); path.lineTo(18, 9); path.lineTo(8, 19); path.close();
                path.moveTo(13, 4); path.lineTo(16, 1); path.lineTo(23, 8); path.lineTo(20, 11); break;
        }
        if (!path.isEmpty()) canvas.drawPath(path, paint);
        canvas.restore();
    }
}
