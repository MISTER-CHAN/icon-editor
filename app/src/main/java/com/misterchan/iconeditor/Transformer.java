package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class Transformer {
    private Bitmap bitmap;
    private double aspectRatio;
    private float centerHorizontal, centerVertical;
    private float translationX, translationY;

    public Transformer(Bitmap bitmap, float translationX, float translationY) {
        this.bitmap = bitmap;
        translateTo(translationX, translationY);
    }

    public void calculateAspectRatio(Positions positions) {
        aspectRatio =
                (double) (positions.right - positions.left + 1) / (double) (positions.bottom - positions.top + 1);
    }

    public void calculateByLocation(Positions positions) {
        calculateAspectRatio(positions);
        calculateCenter(positions);
    }

    public void calculateCenter(Positions positions) {
        centerHorizontal = (positions.left + positions.right + 1) / 2.0f;
        centerVertical = (positions.top + positions.bottom + 1) / 2.0f;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public float getCenterHorizontal() {
        return centerHorizontal;
    }

    public float getCenterVertical() {
        return centerVertical;
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public float getTranslationX() {
        return translationX;
    }

    public float getTranslationY() {
        return translationY;
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

    public void resize(int width, int height, float translationX, float translationY) {
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        new Canvas(bm).drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, width, height),
                new Paint() {
                    {
                        setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    }
                });
        bitmap.recycle();
        bitmap = bm;
        translateTo(translationX, translationY);
    }

    public void translateBy(float x, float y) {
        translationX += x;
        translationY += y;
    }

    public void translateTo(float x, float y) {
        translationX = x;
        translationY = y;
    }
}
