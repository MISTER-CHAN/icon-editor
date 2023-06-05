package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

class DrawingPrimitivePreview implements FloatingLayer {
    private Bitmap bitmap;
    private Canvas canvas;

    public void erase() {
        bitmap.eraseColor(Color.TRANSPARENT);
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
