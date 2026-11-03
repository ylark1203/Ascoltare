package com.ascoltare.music;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

public class CoverView extends View {
    private final String type;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public CoverView(Context context, String type) {
        super(context);
        this.type = type;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w == 0 || h == 0) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);

        switch (type) {
            case "sun": background(canvas, w, h, 0xFFFFCD63, 0xFFD75D31); sun(canvas, w, h); break;
            case "blue": background(canvas, w, h, 0xFF172D4B, 0xFF296C96); blue(canvas, w, h); break;
            case "mono": background(canvas, w, h, 0xFF393B3A, 0xFF101111); mono(canvas, w, h); break;
            case "pink": background(canvas, w, h, 0xFFB33769, 0xFFF4A19C); pink(canvas, w, h); break;
            case "mix": background(canvas, w, h, 0xFFEF9B3C, 0xFF763D79); mix(canvas, w, h); break;
            case "forest": background(canvas, w, h, 0xFF719875, 0xFF142C28); forest(canvas, w, h); break;
            case "daily": background(canvas, w, h, 0xFF1A6742, 0xFF081E17); daily(canvas, w, h); break;
            case "violet": background(canvas, w, h, 0xFF342450, 0xFF6D3A7E); label(canvas, "VELVET", w * .1f, h * .56f, w * .13f, Color.WHITE); break;
            case "lime": background(canvas, w, h, 0xFF38CE68, 0xFF127335); heart(canvas, w, h); break;
            default: background(canvas, w, h, 0xFF20132E, 0xFFB24B44); night(canvas, w, h); break;
        }
    }

    private void background(Canvas canvas, float w, float h, int start, int end) {
        paint.setShader(new LinearGradient(0, 0, w, h, start, end, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
    }

    private void night(Canvas canvas, float w, float h) {
        paint.setColor(0x55F9A48A);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w * .008f);
        for (int i = 0; i < 4; i++) canvas.drawCircle(w * 1.18f, h * 1.08f, w * (.38f + i * .1f), paint);
        paint.setStyle(Paint.Style.FILL);
        label(canvas, "AFTER", w * .12f, h * .32f, w * .18f, Color.WHITE);
        label(canvas, "HOURS", w * .12f, h * .49f, w * .18f, Color.WHITE);
        label(canvas, "VOL. 03", w * .12f, h * .9f, w * .045f, 0xAAFFFFFF);
    }

    private void sun(Canvas canvas, float w, float h) {
        paint.setColor(0xFFFFE7AA);
        canvas.drawCircle(w * .82f, h * .15f, w * .29f, paint);
        paint.setColor(0xFFAC432F);
        path.reset();
        path.moveTo(0, h * .76f); path.lineTo(w, h * .65f); path.lineTo(w, h); path.lineTo(0, h); path.close();
        canvas.drawPath(path, paint);
        label(canvas, "GOLDEN", w * .1f, h * .77f, w * .13f, 0xFF52230E);
        label(canvas, "HOUR", w * .1f, h * .9f, w * .13f, 0xFF52230E);
    }

    private void blue(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w * .007f);
        paint.setColor(0x88B7E4F9);
        canvas.drawRect(w * .52f, h * .16f, w * .9f, h * .73f, paint);
        paint.setStyle(Paint.Style.FILL);
        label(canvas, "BLUE", w * .12f, h * .32f, w * .19f, Color.WHITE);
        label(canvas, "ROOM", w * .12f, h * .5f, w * .19f, Color.WHITE);
        label(canvas, "A SIDE", w * .12f, h * .9f, w * .045f, 0xAAFFFFFF);
    }

    private void mono(Canvas canvas, float w, float h) {
        paint.setColor(0x18FFFFFF);
        for (float y = 0; y < h; y += h / 12) canvas.drawRect(0, y, w, y + 1, paint);
        label(canvas, "NO", w * .12f, h * .32f, w * .2f, Color.WHITE);
        label(canvas, "SIGNAL", w * .12f, h * .5f, w * .17f, Color.WHITE);
        label(canvas, "EST. 2024", w * .12f, h * .9f, w * .04f, 0xAAFFFFFF);
    }

    private void pink(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w * .008f);
        paint.setColor(0x77FFE0E7);
        for (int i = 0; i < 3; i++) canvas.drawCircle(w, 0, w * (.35f + i * .12f), paint);
        paint.setStyle(Paint.Style.FILL);
        label(canvas, "SLOW", w * .12f, h * .34f, w * .18f, Color.WHITE);
        label(canvas, "DANCE", w * .12f, h * .51f, w * .16f, Color.WHITE);
    }

    private void mix(Canvas canvas, float w, float h) {
        label(canvas, "daily", w * .09f, h * .27f, w * .25f, 0xFF351B35);
        label(canvas, "mix", w * .09f, h * .49f, w * .25f, 0xFF351B35);
        label(canvas, "01", w * .53f, h * .89f, w * .4f, 0xFFFFD2A8);
    }

    private void forest(Canvas canvas, float w, float h) {
        paint.setColor(0x20DCF4D5);
        for (int i = -3; i < 8; i++) {
            path.reset(); path.moveTo(w * i / 6, 0); path.lineTo(w * (i + 3) / 6, h); path.lineTo(w * (i + 3.3f) / 6, h); path.lineTo(w * (i + .3f) / 6, 0); path.close(); canvas.drawPath(path, paint);
        }
        label(canvas, "INTO", w * .12f, h * .34f, w * .17f, Color.WHITE);
        label(canvas, "THE WILD", w * .12f, h * .51f, w * .13f, Color.WHITE);
    }

    private void daily(Canvas canvas, float w, float h) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w * .008f);
        paint.setColor(0x66D1FFB9);
        canvas.drawOval(-w * .3f, h * .12f, w * .9f, h * .72f, paint);
        paint.setStyle(Paint.Style.FILL);
        label(canvas, "DAILY", w * .1f, h * .15f, w * .05f, 0xFFD1FFC2);
        label(canvas, "24", w * .48f, h * .82f, w * .48f, 0xFFE2FFD4);
    }

    private void heart(Canvas canvas, float w, float h) {
        paint.setColor(Color.WHITE);
        path.reset();
        path.moveTo(w * .5f, h * .75f);
        path.cubicTo(w * .42f, h * .66f, w * .2f, h * .51f, w * .2f, h * .36f);
        path.cubicTo(w * .2f, h * .16f, w * .43f, h * .15f, w * .5f, h * .31f);
        path.cubicTo(w * .57f, h * .15f, w * .8f, h * .16f, w * .8f, h * .36f);
        path.cubicTo(w * .8f, h * .51f, w * .58f, h * .66f, w * .5f, h * .75f);
        canvas.drawPath(path, paint);
    }

    private void label(Canvas canvas, String value, float x, float y, float size, int color) {
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(value, x, y, paint);
    }
}
