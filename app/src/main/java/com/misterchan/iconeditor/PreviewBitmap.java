package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
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

    @ColorInt
    private int[] pixels;

    public PreviewBitmap(Bitmap bitmap, Rect rect) {
        final int w = rect.width(), h = rect.height();
        this.bitmap = Bitmap.createBitmap(bitmap);
        bm = Bitmap.createBitmap(bitmap, rect.left, rect.top, w, h);
        canvas = new Canvas(this.bitmap);
        cv = new Canvas(bm);
        pixels = new int[w * h];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        this.rect = rect;
    }

    public void addColorFilter(@Size(20) float[] colorMatrix) {
        final int w = bm.getWidth(), h = bm.getHeight(), area = w * h;
        final int[] src = getPixels(), dst = new int[area];
        BitmapUtil.addColorFilter(src, dst, area, colorMatrix);
        setPixels(dst, w, h);
    }

    public void addColorFilter(float scale, float shift) {
        final int w = bm.getWidth(), h = bm.getHeight(), area = w * h;
        final int[] src = getPixels(), dst = new int[area];
        BitmapUtil.addColorFilter(src, dst, area, scale, shift);
        setPixels(dst, w, h);
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
        paint.setBlendMode(BlendMode.SRC_IN);
        paint.setColor(color);
        canvas.drawRect(rect, paint);
    }

    public Bitmap getEntire() {
        return bitmap;
    }

    public int getHeight() {
        return bm.getHeight();
    }

    public Bitmap getOriginal() {
        return bm;
    }

    @ColorInt
    public int[] getPixels() {
        return pixels;
    }

    @ColorInt
    public int[] getPixels(int width, int height, int area) {
        final int[] pixels = new int[area];
        bm.getPixels(pixels, 0, width, 0, 0, width, height);
        return pixels;
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

    public void setPixels(@ColorInt int[] pixels, int width, int height) {
        setPixels(pixels, 0, width, 0, 0, width, height);
    }

    public void setPixels(@ColorInt int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        bitmap.setPixels(pixels, offset, stride,
                rect.left + x, rect.top + y, width, height);
    }
}
