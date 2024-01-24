package com.misterchan.iconeditor.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.misterchan.iconeditor.util.BitmapUtils;

public class MagicPaint {
    private MagicPaint() {
    }

    public static void draw(Canvas canvas, Bitmap source,
                            int threshold,
                            int left, int top, int right, int bottom,
                            int startX, int startY, int stopX, int stopY,
                            Paint magicPaint, Paint paint) {
        final int width = right - left, height = bottom - top;
        final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas cLine = new Canvas(bLine);
        cLine.drawLine(startX, startY, stopX, stopY, paint);
        if (threshold < 0xFF) {
            final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            final Canvas cv = new Canvas(bm);
            cv.drawBitmap(bLine, 0.0f, 0.0f, BitmapUtils.PAINT_SRC);
            cv.drawBitmap(source,
                    new Rect(left, top, right, bottom), new Rect(0, 0, width, height),
                    BitmapUtils.PAINT_SRC_IN);
            final Bitmap bThr = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); // Threshold
            BitmapUtils.floodFill(bm, null, bThr, null,
                    stopX, stopY, Color.BLACK, true, threshold);
            bm.recycle();
            cLine.drawBitmap(bThr, 0.0f, 0.0f, BitmapUtils.PAINT_DST_IN);
            bThr.recycle();
        }
        canvas.drawBitmap(bLine, left, top, magicPaint);
        bLine.recycle();
    }
}
