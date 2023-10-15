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
}
