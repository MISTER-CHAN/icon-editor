package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class Transformer {

    private final static Paint PAINT = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    private Bitmap bitmap;
    private double aspectRatio;
    private float centerX, centerY;
    private float translationX, translationY;

    public Transformer(Bitmap bitmap, float translationX, float translationY) {
        this.bitmap = bitmap;
        translateTo(translationX, translationY);
    }

    public void calculateAspectRatio(Rect rect) {
        aspectRatio =
                (double) (rect.right - rect.left + 1) / (double) (rect.bottom - rect.top + 1);
    }

    public void calculateByLocation(Rect rect) {
        calculateAspectRatio(rect);
        calculateCenter(rect);
    }

    public void calculateCenter(Rect rect) {
        centerX = (rect.left + rect.right + 1) / 2.0f;
        centerY = (rect.top + rect.bottom + 1) / 2.0f;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public Bitmap getBitmap() {
        return bitmap;
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

    public void rotate() {

    }

    public void stretch(int width, int height, float translationX, float translationY) {
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        new Canvas(bm).drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, width, height),
                PAINT);
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