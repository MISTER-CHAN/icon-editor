package com.misterchan.iconeditor.tool;

import android.graphics.Paint;
import android.graphics.Rect;

public class TextTool {
    public int x, y;
    private final Rect bounds = new Rect();
    private final Rect rect = new Rect();
    public String s = "";

    public Rect bounds(Paint paint, float outset) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        rect.set(bounds);
        rect.offset(switch (paint.getTextAlign()) {
            case LEFT -> 0;
            case CENTER -> -(bounds.right >> 1);
            case RIGHT -> -bounds.right;
        } + x, (int) Math.floor(y + fm.top));
        int o = (int) Math.ceil(outset);
        rect.inset(-o, -o);
        return rect;
    }

    public Rect measure(Paint paint, float outset) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        bounds.set(0, 0, (int) Math.ceil(paint.measureText(s)), (int) Math.ceil(fm.bottom - fm.top));
        rect.set(bounds);
        rect.offset(switch (paint.getTextAlign()) {
            case LEFT -> 0;
            case CENTER -> -(bounds.right >> 1);
            case RIGHT -> -bounds.right;
        } + x, (int) Math.floor(y + fm.top));
        int o = (int) Math.ceil(outset);
        rect.inset(-o, -o);
        return rect;
    }

    public Rect rect() {
        return rect;
    }
}
