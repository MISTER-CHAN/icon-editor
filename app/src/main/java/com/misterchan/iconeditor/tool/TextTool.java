package com.misterchan.iconeditor.tool;

import android.graphics.Paint;
import android.graphics.Rect;

public class TextTool {
    public int x, y;
    private final Rect bounds = new Rect();
    private final Rect rect = new Rect();
    public String s = "";

    public Rect getMeasuredBounds(Paint paint, float outset) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return offset(paint.getTextAlign(), fm.top, outset);
    }

    public Rect measure(Paint paint, float outset) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        bounds.set(0, 0, (int) Math.ceil(paint.measureText(s)), (int) Math.ceil(fm.bottom - fm.top));
        return offset(paint.getTextAlign(), fm.top, outset);
    }

    private Rect offset(Paint.Align align, float top, float outset) {
        Rect r = new Rect(rect);
        rect.set(bounds);
        rect.offset(switch (align) {
            case LEFT -> 0;
            case CENTER -> -(bounds.right >> 1);
            case RIGHT -> -bounds.right;
        } + x, (int) Math.floor(y + top));
        int o = (int) Math.ceil(outset);
        rect.inset(-o, -o);
        r.union(rect);
        return r;
    }

    public Rect rect() {
        return rect;
    }
}
