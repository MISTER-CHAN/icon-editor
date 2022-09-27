package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
        Paint paint = new Paint();
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

    private int inRangeFrom0To255(int a) {
        return Math.max(Math.min(a, 255), 0);
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

    public void setFilter(@Size(20) float[] colorMatrix) {
        int w = bm.getWidth(), h = bm.getHeight(), area = w * h;
        int[] pixels = new int[area];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            final int r = Color.red(pixels[i]), g = Color.green(pixels[i]), b = Color.blue(pixels[i]),
                    a = Color.alpha(pixels[i]);
            final int r_ = inRangeFrom0To255((int) (r * colorMatrix[0] + g * colorMatrix[1] + b * colorMatrix[2] + a * colorMatrix[3] + colorMatrix[4]));
            final int g_ = inRangeFrom0To255((int) (r * colorMatrix[5] + g * colorMatrix[6] + b * colorMatrix[7] + a * colorMatrix[8] + colorMatrix[9]));
            final int b_ = inRangeFrom0To255((int) (r * colorMatrix[10] + g * colorMatrix[11] + b * colorMatrix[12] + a * colorMatrix[13] + colorMatrix[14]));
            final int a_ = inRangeFrom0To255((int) (r * colorMatrix[15] + g * colorMatrix[16] + b * colorMatrix[17] + a * colorMatrix[18] + colorMatrix[19]));
            pixels[i] = Color.argb(a_, r_, g_, b_);
        }
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    public void setFilter(float scale, float shift) {
        int w = bm.getWidth(), h = bm.getHeight(), area = w * h;
        int[] pixels = new int[area];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            final int r = Color.red(pixels[i]), g = Color.green(pixels[i]), b = Color.blue(pixels[i]),
                    a = Color.alpha(pixels[i]);
            final int r_ = inRangeFrom0To255((int) (r * scale + shift));
            final int g_ = inRangeFrom0To255((int) (g * scale + shift));
            final int b_ = inRangeFrom0To255((int) (b * scale + shift));
            pixels[i] = Color.argb(a, r_, g_, b_);
        }
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    public void setPixels(@ColorInt int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        bitmap.setPixels(pixels, offset, stride,
                rect.left + x, rect.top + y, width, height);
    }
}
