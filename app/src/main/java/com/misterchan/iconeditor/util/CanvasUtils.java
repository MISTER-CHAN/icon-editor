package com.misterchan.iconeditor.util;

import android.graphics.Canvas;
import android.graphics.Paint;

public class CanvasUtils {
    private CanvasUtils() {
    }

    public static void drawInclusiveLine(Canvas canvas, int startX, int startY, int stopX, int stopY, Paint paint) {
        if (startX <= stopX) ++stopX;
        else ++startX;
        if (startY <= stopY) ++stopY;
        else ++startY;

        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    public static void drawRightAngles(Canvas canvas, float l, float t, float r, float b, Paint paint) {
        canvas.drawLine(l, t, l - 100.0f, t, paint);
        canvas.drawLine(r, t, r + 100.0f, t, paint);
        canvas.drawLine(r, t - 100.0f, r, t, paint);
        canvas.drawLine(r, b, r, b + 100.0f, paint);
        canvas.drawLine(r + 100.0f, b, r, b, paint);
        canvas.drawLine(l, b, l - 100.0f, b, paint);
        canvas.drawLine(l, b + 100.0f, l, b, paint);
        canvas.drawLine(l, t, l, t - 100.0f, paint);
    }
}
