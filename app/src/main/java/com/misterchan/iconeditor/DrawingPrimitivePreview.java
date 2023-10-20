package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.misterchan.iconeditor.util.BitmapUtils;

public class DrawingPrimitivePreview implements FloatingLayer {
    private Bitmap bitmap;
    private Canvas canvas;

    public void erase() {
        if (bitmap == null) return;
        bitmap.eraseColor(Color.TRANSPARENT);
    }

    public void erase(Rect rect) {
        canvas.drawRect(rect, BitmapUtils.PAINT_CLEAR);
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isRecycled() {
        return bitmap == null;
    }

    public void recycle() {
        if (bitmap == null) {
            return;
        }
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
