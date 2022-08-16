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
        this.rect = rect;
    }

    private void draw() {
        canvas.drawBitmap(bm, rect.left, rect.top, paint);
    }

    public Bitmap getBitmap() {
        return bitmap;
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
