package com.misterchan.iconeditor.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import androidx.annotation.ColorInt;

import com.misterchan.iconeditor.util.BitmapUtils;

public class MagicEraser {
    public Point b, f; // Background and foreground
    public final PointF bd = new PointF(0.0f, 0.0f), fd = new PointF(0.0f, 0.0f); // Distance

    public void eraseImprecisely(Canvas canvas, Bitmap source,
                                 @ColorInt int foregroundColor, @ColorInt int backgroundColor,
                                 int left, int top, int right, int bottom,
                                 float startX, float startY, float stopX, float stopY,
                                 Paint paint) {
        final int width = right - left, height = bottom - top;
        final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas cLine = new Canvas(bLine);
        cLine.drawLine(startX, startY, stopX, stopY, paint);
        canvas.drawBitmap(bLine, left, top, BitmapUtils.PAINT_DST_OUT);
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        new Canvas(bm).drawBitmap(source,
                new Rect(left, top, right, bottom), new Rect(0, 0, width, height), BitmapUtils.PAINT_SRC);
        BitmapUtils.removeBackground(bm, foregroundColor, backgroundColor);
        cLine.drawBitmap(bm, 0.0f, 0.0f, BitmapUtils.PAINT_SRC_IN);
        bm.recycle();
        canvas.drawBitmap(bLine, left, top, BitmapUtils.PAINT_SRC_OVER);
        bLine.recycle();
    }

    public void erasePrecisely(Canvas canvas, Bitmap source,
                               @ColorInt int foregroundColor, @ColorInt int backgroundColor,
                               int left, int top, int right, int bottom,
                               Paint paint) {
        final int width = right - left, height = bottom - top;
        final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas cLine = new Canvas(bLine);
        cLine.drawLine(b.x - left, b.y - top,
                f.x - left, f.y - top,
                paint);
        canvas.drawBitmap(bLine, left, top, BitmapUtils.PAINT_DST_OUT);
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        new Canvas(bm).drawBitmap(source,
                new Rect(left, top, right, bottom), new Rect(0, 0, width, height),
                BitmapUtils.PAINT_SRC);
        BitmapUtils.removeBackground(bm, foregroundColor, backgroundColor);
        cLine.drawBitmap(bm, 0.0f, 0.0f, BitmapUtils.PAINT_SRC_IN);
        bm.recycle();
        canvas.drawBitmap(bLine, left, top, BitmapUtils.PAINT_SRC_OVER);
        bLine.recycle();
    }
}
