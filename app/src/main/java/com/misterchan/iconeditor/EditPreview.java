package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;

import com.misterchan.iconeditor.util.BitmapUtils;

public class EditPreview {
    public interface Modification {
        void modify(@ColorInt int[] src, @ColorInt int[] dst);
    }

    private final Bitmap bitmap;
    private final Bitmap src;
    private final Bitmap bm;
    private boolean cachePixels;
    private boolean committed;
    private final Canvas canvas;
    private final Rect rect;

    @ColorInt
    private int[] pixels;

    /**
     * Previewing rectangle
     */
    private Rect prevRect;

    /**
     * @param prevRect Previewing rectangle
     */
    public EditPreview(Bitmap bitmap, Rect rect, boolean cacheBitmap, boolean cachePixels, Rect prevRect) {
        src = bitmap;
        this.bitmap = Bitmap.createBitmap(bitmap);
        canvas = new Canvas(this.bitmap);
        this.rect = rect;
        this.cachePixels = cachePixels;

        if (prevRect == null) {
            prevRect = rect;
        }
        this.prevRect = prevRect;
        if (rect != prevRect && !prevRect.intersect(rect)) {
            prevRect.setEmpty();
        }

        bm = cacheBitmap
                ? Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
                : null;
    }

    public boolean committed() {
        return committed;
    }

    public void drawBitmap(Bitmap bm) {
        canvas.drawBitmap(bm, rect.left, rect.top, BitmapUtils.PAINT_SRC_OVER);
    }

    public void drawColor(@ColorInt int color, BlendMode blendMode) {
        final Paint paint = new Paint();
        paint.setBlendMode(blendMode);
        paint.setColor(color);
        canvas.drawRect(rect, paint);
    }

    public void edit(Modification mod) {
        final int[] src = getPixels(), dst = cachePixels ? new int[prevRect.width() * prevRect.height()] : src;
        mod.modify(src, dst);
        setPixels(dst);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Bitmap getEntire() {
        return bitmap;
    }

    public Bitmap getOriginal() {
        return src;
    }

    @ColorInt
    public int[] getOriginalPixels() {
        return BitmapUtils.getPixels(src, rect);
    }

    @ColorInt
    public int[] getPixels() {
        if (cachePixels) {
            if (pixels == null) {
                final int vw = prevRect.width(), vh = prevRect.height();
                pixels = new int[vw * vh];
                if (pixels.length > 0) {
                    src.getPixels(pixels, 0, vw, prevRect.left, prevRect.top, vw, vh);
                }
            }
            return pixels;
        } else {
            final int w = prevRect.width(), h = prevRect.height();
            final int[] pixels = new int[w * h];
            src.getPixels(pixels, 0, w, prevRect.left, prevRect.top, w, h);
            return pixels;
        }
    }

    public Rect getRect() {
        return rect;
    }

    public void prepareToCommit() {
        cachePixels = false;
        prevRect = rect;
        committed = true;
        pixels = null;
    }

    public void recycle() {
        bitmap.recycle();

        if (bm != null) {
            bm.recycle();
        }
    }

    public void revert() {
        canvas.drawBitmap(src, rect, rect, BitmapUtils.PAINT_SRC);
    }

    public void setBitmap(Bitmap bm) {
        canvas.drawBitmap(bm, rect.left, rect.top, BitmapUtils.PAINT_SRC);
    }

    public void setPixels(@ColorInt int[] pixels) {
        setPixels(pixels, prevRect.left, prevRect.top, prevRect.width(), prevRect.height());
    }

    private void setPixels(@ColorInt int[] pixels, int x, int y, int width, int height) {
        bitmap.setPixels(pixels, 0, width, x, y, width, height);
    }

    public void transform(Matrix matrix) {
        canvas.drawBitmap(src, 0.0f, 0.0f, BitmapUtils.PAINT_SRC);
        canvas.drawRect(rect, BitmapUtils.PAINT_CLEAR);
        matrix.postTranslate(rect.left, rect.top);
        canvas.drawBitmap(bm, matrix, BitmapUtils.PAINT_SRC);
    }

    public boolean visible() {
        return pixels == null || pixels.length > 0 || !cachePixels;
    }
}
