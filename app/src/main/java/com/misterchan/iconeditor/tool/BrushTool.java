package com.misterchan.iconeditor.tool;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;

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

    private Bitmap src, dst, brush;
    public final Rect rect = new Rect();
    public TipShape tipShape = TipShape.BRUSH;

    public Bitmap bm() {
        return dst;
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
        set(src, color);
    }

    private void set(@NonNull Bitmap src, long color) {
        if (tipShape == TipShape.REF && this.src != null && !this.src.isRecycled())
            this.src.recycle();
        this.src = src;

        final Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        dst.eraseColor(color);
        new Canvas(dst).drawBitmap(src, 0.0f, 0.0f, PAINT);
        if (this.dst != null) this.dst.recycle();
        this.dst = dst;
        rect.right = dst.getWidth();
        rect.bottom = dst.getHeight();
    }

    public void recycle() {
        if (src != null) {
            src.recycle();
            src = null;
        }
        if (dst != null) {
            dst.recycle();
            dst = null;
        }
    }

    public void recycleAll() {
        recycle();
        if (brush != null && !brush.isRecycled()) brush.recycle();
    }

    public boolean recycled() {
        return dst == null;
    }
}
