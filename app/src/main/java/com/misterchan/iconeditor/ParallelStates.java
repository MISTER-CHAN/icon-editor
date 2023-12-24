package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.misterchan.iconeditor.util.BitmapUtils;

public class ParallelStates {
    private Bitmap bitmap;
    private Canvas canvas;
    public final Rect bounds = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public Bitmap bitmap() {
        return bitmap;
    }

    public boolean areBoundsEmpty() {
        return bounds.isEmpty();
    }

    public boolean doBoundsIntersect() {
        return bounds.intersects(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public void draw(Bitmap bm, Rect dst) {
        canvas.drawBitmap(bm, null, dst, BitmapUtils.PAINT_SRC);
    }

    public void drawOnto(Canvas canvas) {
        if (bitmap == null || !bounds.intersects(0, 0, bitmap.getWidth(), bitmap.getHeight())) {
            return;
        }
        canvas.drawBitmap(bitmap, bounds, bounds, BitmapUtils.PAINT_SRC);
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

    private void resetBounds() {
        bounds.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public void set(Bitmap src) {
        if (bitmap != null) bitmap.recycle();
        bitmap = Bitmap.createBitmap(src);
        canvas = new Canvas(bitmap);
    }

    public void unionBounds(int x0, int y0, int x1, int y1, float radius) {
        boolean x = x0 <= x1, y = y0 <= y1;
        int rad = (int) Math.ceil(radius);
        unionBounds((x ? x0 : x1) - rad, (y ? y0 : y1) - rad, (x ? x1 : x0) + rad, (y ? y1 : y0) + rad);
    }

    public void unionBounds(Rect rect) {
        unionBounds(rect.left, rect.top, rect.right, rect.bottom);
    }

    public void unionBounds(int left, int top, int right, int bottom) {
        if (bitmap == null) return;
        bounds.union(left, top, right, bottom);
    }
}
