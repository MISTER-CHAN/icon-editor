package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

class DrawingPrimitivePreview implements FloatingLayer {
    private static final Paint PAINT_CLEAR = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.CLEAR);
            setFilterBitmap(false);
        }
    };

    private Bitmap bitmap;
    private Canvas canvas;

    public void erase() {
        if (bitmap == null) return;
        bitmap.eraseColor(Color.TRANSPARENT);
    }

    public void erase(Rect rect) {
        canvas.drawRect(rect, PAINT_CLEAR);
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public int getLeft() {
        return 0;
    }

    @Override
    public int getTop() {
        return 0;
    }

    public boolean isRecycled() {
        return bitmap == null;
    }

    public void recycle() {
        bitmap.recycle();
        bitmap = null;
        canvas = null;
    }

    public void setBitmap(int width, int height) {
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }
}
