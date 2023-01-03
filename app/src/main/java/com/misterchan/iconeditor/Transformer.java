package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class Transformer {

    private final static Paint PAINT = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    private Bitmap bitmap;
    private double aspectRatio;
    private float centerX, centerY;
    private final RectF dpb = new RectF(); // Distance from point to bounds
    private Rect rect;

    public Transformer(Bitmap bitmap, Rect rect) {
        this.bitmap = bitmap;
        this.rect = rect;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void calculateAspectRatio() {
        aspectRatio = (double) rect.width() / (double) rect.height();
    }

    public void calculateByLocation() {
        calculateAspectRatio();
        calculateCenter();
    }

    public void calculateCenter() {
        centerX = rect.exactCenterX();
        centerY = rect.exactCenterY();
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public RectF getDpb() {
        return dpb;
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public void recycle() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void rotate(float degrees) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final float semiWidth = w / 2.0f, semiHeight = h / 2.0f;
        final float diagonal = (float) Math.sqrt(w * w + h * h), semiDiag = diagonal / 2.0f;
        final Bitmap bm = Bitmap.createBitmap((int) Math.ceil(diagonal), (int) Math.ceil(diagonal), Bitmap.Config.ARGB_8888);
        final Matrix matrix = new Matrix();
        matrix.setTranslate((float) Math.ceil(semiDiag - semiWidth), (float) Math.ceil(semiDiag - semiHeight));
        matrix.postRotate(degrees, semiDiag, semiDiag);
        new Canvas(bm).drawBitmap(bitmap, matrix, PAINT);
        bitmap.recycle();
        bitmap = bm;
    }

    public void stretch(int width, int height) {
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        new Canvas(bm).drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, width, height),
                PAINT);
        bitmap.recycle();
        bitmap = bm;
    }
}
