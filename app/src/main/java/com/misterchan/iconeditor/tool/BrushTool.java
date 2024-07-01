package com.misterchan.iconeditor.tool;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.misterchan.iconeditor.util.BitmapUtils;

public class BrushTool {
    public enum TipShape {
        PRESET_BRUSH, CLIP;

        private static final TipShape[] values = values();

        public static TipShape valueAt(int ordinal) {
            return values[ordinal];
        }
    }

    private static final Paint PAINT = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.DST_IN);
            setFilterBitmap(false);
        }
    };

    private Bitmap src, dst, brush;
    public TipShape tipShape = TipShape.PRESET_BRUSH;

    public Bitmap bm() {
        return dst;
    }

    public void setBrush(Bitmap brush) {
        this.brush = brush;
    }

    public void setToBrush(long color) {
        if (brush == null) return;
        set(brush, color);
        tipShape = TipShape.PRESET_BRUSH;
    }

    public void setToClip(Bitmap src, long color) {
        if (src == null) return;
        set(src, color);
        tipShape = TipShape.CLIP;
    }

    public void set(long color) {
        set(src, color);
    }

    private void set(@NonNull Bitmap src, long color) {
        if (src != this.src) {
            if (this.src != null) this.src.recycle();
            this.src = BitmapUtils.createBitmap(src);
        }
        final Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        dst.eraseColor(color);
        new Canvas(dst).drawBitmap(src, 0.0f, 0.0f, PAINT);
        if (this.dst != null) this.dst.recycle();
        this.dst = dst;
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
        if (brush != null) brush.recycle();
    }

    public boolean recycled() {
        return dst == null;
    }
}
