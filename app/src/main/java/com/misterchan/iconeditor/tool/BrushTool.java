package com.misterchan.iconeditor.tool;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class BrushTool {
    public enum TipShape {
        BRUSH, REF
    }

    private static final Paint PAINT = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.DST_IN);
            setFilterBitmap(false);
        }
    };

    private Bitmap bitmap, brush;
    public final Rect rect = new Rect();
    public TipShape tipShape = TipShape.BRUSH;

    public Bitmap bm() {
        return bitmap;
    }

    public void setBrush(Bitmap brush) {
        this.brush = brush;
    }

    public void setToBrush(long color) {
        if (brush == null) return;
        set(brush, color);
        tipShape = TipShape.BRUSH;
    }

    public void setToRef(Bitmap src, long color) {
        set(src, color);
        tipShape = TipShape.REF;
    }

    public void set(long color) {
        set(bitmap, color);
    }

    private void set(Bitmap src, long color) {
        if (src == null) src = bm();

        final Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        dst.eraseColor(color);
        new Canvas(dst).drawBitmap(src, 0.0f, 0.0f, PAINT);
        recycle();
        bitmap = dst;
        rect.right = dst.getWidth();
        rect.bottom = dst.getHeight();
    }

    public void recycle() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void recycleAll() {
        recycle();
        if (brush != null) brush.recycle();
    }

    public boolean recycled() {
        return bitmap == null;
    }
}
