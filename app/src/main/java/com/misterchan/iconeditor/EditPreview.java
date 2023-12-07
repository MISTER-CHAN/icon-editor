package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.Size;

import com.misterchan.iconeditor.util.BitmapUtils;

public class EditPreview {
    private Bitmap bitmap;
    private Bitmap bm;
    private final Bitmap src;
    private Canvas canvas;
    private Canvas cv;
    private final Rect rect, visibleRect;

    @ColorInt
    private final int[] pixels, visiblePixels;

    public EditPreview(Bitmap bitmap, Rect rect) {
        this(bitmap, rect, null);
    }

    public EditPreview(Bitmap bitmap, Rect rect, Rect visibleRect) {
        final int w = rect.width(), h = rect.height();
        src = bitmap;
        this.bitmap = Bitmap.createBitmap(bitmap);
        bm = Bitmap.createBitmap(w, h, bitmap.getConfig(), bitmap.hasAlpha(), bitmap.getColorSpace());
        canvas = new Canvas(this.bitmap);
        cv = new Canvas(bm);
        cv.drawBitmap(bitmap, rect, new RectF(0.0f, 0.0f, w, h), BitmapUtils.PAINT_SRC);
        this.rect = rect;
        pixels = new int[w * h];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        if (visibleRect != null) {
            if (!visibleRect.intersect(rect)) {
                visibleRect.setEmpty();
            }
            if (!visibleRect.contains(rect)) {
                this.visibleRect = visibleRect;
                final int vw = visibleRect.width(), vh = visibleRect.height();
                visiblePixels = new int[vw * vh];
                if (visible()) {
                    bm.getPixels(visiblePixels, 0, vw, visibleRect.left, visibleRect.top, vw, vh);
                }
            } else {
                this.visibleRect = rect;
                visiblePixels = pixels;
            }
        } else {
            this.visibleRect = rect;
            visiblePixels = pixels;
        }
    }

    public void addLightingColorFilter(float mul, float add, boolean stopped) {
        final int[] src = getPixels(stopped), dst = new int[getArea(stopped)];
        BitmapUtils.addLightingColorFilter(src, dst, mul, add);
        setPixels(dst, stopped);
    }

    public void addLightingColorFilter(@Size(8) float[] lighting) {
        final int[] src = getPixels(true), dst = new int[getArea(true)];
        BitmapUtils.addLightingColorFilter(src, dst, lighting);
        setPixels(dst, true);
    }

    public void addColorMatrixColorFilter(@Size(20) float[] colorMatrix) {
        final int[] src = getPixels(true), dst = new int[getArea(true)];
        BitmapUtils.addColorMatrixColorFilter(src, dst, colorMatrix);
        setPixels(dst, true);
    }

    public void clearFilters() {
        canvas.drawBitmap(bm, rect.left, rect.top, BitmapUtils.PAINT_SRC);
    }

    @ColorInt
    public int[] copyPixels() {
        final int w = bm.getWidth(), h = bm.getHeight();
        final int[] pixels = new int[w * h];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        return pixels;
    }

    public void drawBitmap(Bitmap b) {
        canvas.drawBitmap(b, rect.left, rect.top, BitmapUtils.PAINT_SRC_OVER);
    }

    public void drawColor(@ColorInt int color, BlendMode blendMode) {
        final Paint paint = new Paint();
        paint.setBlendMode(blendMode);
        paint.setColor(color);
        canvas.drawRect(rect, paint);
    }

    public int getArea(boolean stopped) {
        return getWidth(stopped) * getHeight(stopped);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Bitmap getEntire() {
        return bitmap;
    }

    public int getHeight(boolean stopped) {
        return stopped ? bm.getHeight() : visibleRect.height();
    }

    public Bitmap getOriginal() {
        return bm;
    }

    @ColorInt
    public int[] getPixels(boolean stopped) {
        return stopped ? pixels : visiblePixels;
    }

    public Rect getRect() {
        return rect;
    }

    public int getWidth(boolean stopped) {
        return stopped ? bm.getWidth() : visibleRect.width();
    }

    public boolean visible() {
        return visiblePixels.length > 0;
    }

    public void posterize(@IntRange(from = 0x01, to = 0xFF) int level, boolean stopped) {
        final int[] src = getPixels(stopped), dst = new int[getArea(stopped)];
        BitmapUtils.posterize(src, dst, level);
        setPixels(dst, stopped);
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
        canvas.drawBitmap(bm, rect.left, rect.top, BitmapUtils.PAINT_SRC);
    }

    public void setPixels(@ColorInt int[] pixels, boolean stopped) {
        if (stopped) {
            setPixels(pixels, 0, 0, bm.getWidth(), bm.getHeight());
        } else {
            setPixels(pixels, visibleRect.left, visibleRect.top, visibleRect.width(), visibleRect.height());
        }
    }

    private void setPixels(@ColorInt int[] pixels, int x, int y, int width, int height) {
        bitmap.setPixels(pixels, 0, width, rect.left + x, rect.top + y, width, height);
    }

    public void transform(Matrix matrix) {
        canvas.drawBitmap(src, 0.0f, 0.0f, BitmapUtils.PAINT_SRC);
        canvas.drawRect(rect, BitmapUtils.PAINT_CLEAR);
        matrix.postTranslate(rect.left, rect.top);
        canvas.drawBitmap(bm, matrix, BitmapUtils.PAINT_SRC);
    }
}
