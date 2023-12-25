package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.misterchan.iconeditor.util.BitmapUtils;

public class ImageStateAccumulator {
    private Bitmap bitmap;
    private Canvas canvas;
    public final Rect stateBounds = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public boolean areStateBoundsEmpty() {
        return stateBounds.isEmpty();
    }

    public Bitmap bitmap() {
        return bitmap;
    }

    public boolean doStateBoundsIntersect() {
        return stateBounds.intersects(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public void draw(Bitmap bm, Rect dst) {
        canvas.drawBitmap(bm, null, dst, BitmapUtils.PAINT_SRC);
    }

    public void drawOnto(Canvas canvas) {
        if (bitmap == null || !doStateBoundsIntersect()) {
            return;
        }
        canvas.drawBitmap(bitmap, stateBounds, stateBounds, BitmapUtils.PAINT_SRC);
        resetBounds();
    }

    public void erase(Bitmap bm, Rect rect) {
        if (rect != null) {
            canvas.drawBitmap(bm, rect, rect, BitmapUtils.PAINT_SRC);
        } else {
            canvas.drawBitmap(bm, 0.0f, 0.0f, BitmapUtils.PAINT_SRC);
        }
    }

    public boolean isRecycled() {
        return bitmap == null || bitmap.isRecycled();
    }

    public void post(Bitmap bm, Rect rect) {
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bm, rect, rect, BitmapUtils.PAINT_SRC);
        resetBounds();
    }

    public void recycle() {
        if (bitmap == null) return;
        bitmap.recycle();
        bitmap = null;
        canvas = null;
    }

    public void resetBounds() {
        stateBounds.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public void set(Bitmap src) {
        if (bitmap != null) bitmap.recycle();
        bitmap = Bitmap.createBitmap(src);
        canvas = new Canvas(bitmap);
    }

    public void unionBounds(int x0, int y0, int x1, int y1, float radius) {
        boolean x = x0 <= x1, y = y0 <= y1;
        int rad = (int) Math.ceil(radius + 1.0f);
        unionBounds((x ? x0 : x1) - rad, (y ? y0 : y1) - rad, (x ? x1 : x0) + rad + 1, (y ? y1 : y0) + rad + 1);
    }

    public void unionBounds(Rect rect) {
        unionBounds(rect.left, rect.top, rect.right, rect.bottom);
    }

    public void unionBounds(int left, int top, int right, int bottom) {
        if (bitmap == null) return;
        stateBounds.union(left, top, right, bottom);
    }
}
