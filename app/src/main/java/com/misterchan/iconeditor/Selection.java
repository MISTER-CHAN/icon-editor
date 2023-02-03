package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

class Selection {
    private static final Paint PAINT_DST_OUT = new Paint() {
        {
            setBlendMode(BlendMode.DST_OUT);
        }
    };

    private static final Paint PAINT_SRC = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
        }
    };

    public final Bitmap mask;
    public final Canvas canvas;
    public final Rect rect;

    public Selection(Bitmap bitmap) {
        final int width = bitmap.getWidth(), height = bitmap.getHeight();
        mask = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        canvas = new Canvas(bitmap);
        rect = new Rect(0, 0, width, height);
    }

    public Bitmap toSelectedSubset(Bitmap bitmap) {
        new Canvas(bitmap).drawBitmap(mask, rect, rect, PAINT_DST_OUT);
        return bitmap;
    }
}
