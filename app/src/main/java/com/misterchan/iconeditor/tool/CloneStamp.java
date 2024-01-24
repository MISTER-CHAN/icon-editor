package com.misterchan.iconeditor.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import com.misterchan.iconeditor.util.BitmapUtils;

public class CloneStamp {
    public Point src;

    public void paint(Canvas canvas, Bitmap bitmap,
                      float left, float top, int width, int height, int dx, int dy,
                      float startX, float startY, float stopX, float stopY,
                      Paint paint) {
        final int l = (int) (left + dx), t = (int) (top + dy);
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas cv = new Canvas(bm);
        cv.drawLine(startX, startY, stopX, stopY, paint);
        cv.drawRect(0.0f, 0.0f, -l, height, BitmapUtils.PAINT_CLEAR);
        cv.drawRect(0.0f, 0.0f, width, -t, BitmapUtils.PAINT_CLEAR);
        cv.drawRect(bitmap.getWidth() - l, 0.0f, width, height, BitmapUtils.PAINT_CLEAR);
        cv.drawRect(0.0f, bitmap.getHeight() - t, width, height, BitmapUtils.PAINT_CLEAR);
        cv.drawBitmap(bitmap,
                new Rect(l, t, l + width, t + height),
                new RectF(0.0f, 0.0f, width, height),
                BitmapUtils.PAINT_SRC_IN);
        canvas.drawBitmap(bm, left, top, BitmapUtils.PAINT_SRC_OVER);
        bm.recycle();
    }
}
