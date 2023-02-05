package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

class Transformer {

    private final static Paint PAINT = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    private Bitmap bitmap;
    private double aspectRatio;
    private float centerX, centerY;
    private final Rect rect;

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

    public int getHeight() {
        return bitmap.getHeight();
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public Tab makeTab() {
        Tab tab = new Tab();
        tab.bitmap = bitmap;
        tab.left = rect.left;
        tab.top = rect.top;
        return tab;
    }

    public void recycle() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void rotate(float degrees) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final float diagonal = (float) Math.sqrt(w * w + h * h), semiDiag = diagonal / 2.0f;
        final Bitmap bm = Bitmap.createBitmap((int) Math.ceil(diagonal), (int) Math.ceil(diagonal), Bitmap.Config.ARGB_8888);
        final Canvas cv = new Canvas(bm);
        cv.rotate(degrees, semiDiag, semiDiag);
        cv.drawBitmap(bitmap,
                (float) Math.ceil(semiDiag - (w >> 1)), (float) Math.ceil(semiDiag - (h >> 1)),
                PAINT);
        bitmap.recycle();
        bitmap = bm;
    }

    public void stretch(int width, int height) {
        final int absWidth = Math.abs(width), absHeight = Math.abs(height);
        final Bitmap bm = Bitmap.createBitmap(absWidth, absHeight, Bitmap.Config.ARGB_8888);
        final Canvas cv = new Canvas(bm);
        cv.translate(width >= 0 ? 0.0f : absWidth, height >= 0 ? 0.0f : absHeight);
        cv.scale((float) width / (float) bitmap.getWidth(), (float) height / (float) bitmap.getHeight());
        cv.drawBitmap(bitmap, 0.0f, 0.0f, PAINT);
        bitmap.recycle();
        bitmap = bm;
    }
}
