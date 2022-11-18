package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

public class PreviewBitmap {

    private static final Paint PAINT_SRC = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    private static final Paint PAINT = new Paint();

    private Bitmap bitmap;
    private Bitmap bm;
    private Canvas canvas;
    private Canvas cv;
    private final Rect rect;

    public PreviewBitmap(Bitmap bitmap, Rect rect) {
        this.bitmap = Bitmap.createBitmap(bitmap);
        bm = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
        canvas = new Canvas(this.bitmap);
        cv = new Canvas(bm);
        this.rect = rect;
    }

    public void addColorFilter(@Size(20) float[] colorMatrix) {
        BitmapUtil.addColorFilter(bm, 0, 0, bitmap, rect.left, rect.top,
                colorMatrix);
    }

    public void addColorFilter(float scale, float shift) {
        BitmapUtil.addColorFilter(bm, 0, 0, bitmap, rect.left, rect.top,
                scale, shift);
    }

    public void clearFilter() {
        draw();
    }

    private void draw() {
        canvas.drawBitmap(bm, rect.left, rect.top, PAINT_SRC);
    }

    public void drawBitmap(Bitmap b) {
        canvas.drawBitmap(b, rect.left, rect.top, PAINT);
    }

    public void drawColor(@ColorInt int color) {
        final Paint paint = new Paint();
        paint.setColor(color);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(rect, paint);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getHeight() {
        return bm.getHeight();
    }

    public void getPixels(@ColorInt int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        bm.getPixels(pixels, offset, stride, x, y, width, height);
    }

    public int getWidth() {
        return bm.getWidth();
    }

    public void recycle() {
        canvas = null;
        bitmap.recycle();
        bitmap = null;

        cv = null;
        bm.recycle();
        bm = null;
    }

    public void reset() {
        canvas.drawBitmap(bm, rect.left, rect.top, PAINT_SRC);
    }

    public void setPixels(@ColorInt int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        bitmap.setPixels(pixels, offset, stride,
                rect.left + x, rect.top + y, width, height);
    }
}
