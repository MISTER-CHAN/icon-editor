package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

public class BitmapWithFilter {
    private final Bitmap bitmap;
    private final Bitmap bm;
    private final Canvas canvas;
    private final Canvas cv;
    private final Rect rect;

    private final Paint paint = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    public BitmapWithFilter(Bitmap bitmap, Rect rect) {
        this.bitmap = Bitmap.createBitmap(bitmap);
        this.bm = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width() + 1, rect.height() + 1);
        canvas = new Canvas(this.bitmap);
        cv = new Canvas(bm);
        this.rect = rect;
    }

    public void clearFilter() {
        paint.setColorFilter(null);
        draw();
    }

    private void draw() {
        canvas.drawBitmap(bm, rect.left, rect.top, paint);
    }

    public void drawColor(@ColorInt int color) {
        canvas.drawColor(color, PorterDuff.Mode.SRC);
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

    public void override() {
        cv.drawBitmap(bm, 0, 0, paint);
    }

    public void postFilter(ColorMatrix colorMatrix) {
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Rect r = new Rect(rect.left, rect.top, rect.right, rect.bottom);
        canvas.drawBitmap(bitmap, r, r, paint);
    }

    public void recycle() {
        bitmap.recycle();
        bm.recycle();
    }

    public void setFilter(ColorMatrix colorMatrix) {
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        draw();
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

    public void setPixels(@ColorInt int[] pixels, int offset, int stride,
                          int x, int y, int width, int height) {
        bitmap.setPixels(pixels, offset, stride,
                rect.left + x, rect.top + y, width, height);
    }
}
