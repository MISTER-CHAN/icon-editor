package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class BitmapWithFilter {
    private final Bitmap bitmap;
    private final Bitmap bm;
    private final Canvas canvas;
    private final Canvas cv;
    private final Rect rect;

    private final Paint paint = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    public BitmapWithFilter(Bitmap bitmap, Rect rect) {
        this.bitmap = Bitmap.createBitmap(bitmap);
        this.bm = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width() + 1, rect.height() + 1);
        canvas = new Canvas(this.bitmap);
        cv = new Canvas(bm);
        this.rect = rect;
    }

    private void draw() {
        canvas.drawBitmap(bm, rect.left, rect.top, paint);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void override() {
        cv.drawBitmap(bm, 0, 0, paint);
    }

    public void postFilter(ColorMatrix colorMatrix) {
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Rect r = new Rect(rect.left, rect.top, rect.right, rect.bottom);
        canvas.drawBitmap(bitmap, r, r, paint);
    }

    public void recycle() {
        bitmap.recycle();
        bm.recycle();
    }

    public void setFilter(ColorMatrix colorMatrix) {
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        draw();
    }
}
