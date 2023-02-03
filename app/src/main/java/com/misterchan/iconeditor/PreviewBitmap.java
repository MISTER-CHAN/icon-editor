package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

class PreviewBitmap {

    private static final Paint PAINT_SRC = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    private static final Paint PAINT = new Paint();

    private Bitmap bitmap;
    private Bitmap bm;
    private Canvas canvas;
    private Canvas cv;
    private final Rect rect;

    @ColorInt
    private final int[] pixels;

    private final Paint paint = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
        }
    };

    public PreviewBitmap(Bitmap bitmap, Rect rect) {
        final int w = rect.width(), h = rect.height();
        this.bitmap = Bitmap.createBitmap(bitmap);
        bm = Bitmap.createBitmap(w, h, bitmap.getConfig(), true, bitmap.getColorSpace());
        canvas = new Canvas(this.bitmap);
        cv = new Canvas(bm);
        cv.drawBitmap(bitmap, rect, new RectF(0.0f, 0.0f, w, h), paint);
        pixels = new int[w * h];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        this.rect = rect;
    }

    public void addLightingColorFilter(float scale, float shift) {
        final int w = bm.getWidth(), h = bm.getHeight();
        final int[] src = getPixels(), dst = new int[w * h];
        BitmapUtil.addLightingColorFilter(src, dst, scale, shift);
        setPixels(dst, w, h);
    }

    public void addLightingColorFilter(@Size(8) float[] lighting) {
        final int w = bm.getWidth(), h = bm.getHeight();
        final int[] src = getPixels(), dst = new int[w * h];
        BitmapUtil.addLightingColorFilter(src, dst, lighting);
        setPixels(dst, w, h);
    }

    public void addColorMatrixColorFilter(@Size(20) float[] colorMatrix) {
        final int w = bm.getWidth(), h = bm.getHeight();
        final int[] src = getPixels(), dst = new int[w * h];
        BitmapUtil.addColorMatrixColorFilter(src, dst, colorMatrix);
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

    public int getArea() {
        return bm.getWidth() * bm.getHeight();
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
