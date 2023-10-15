package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Reference {
    private Bitmap bitmap;

    public Bitmap bm() {
        return bitmap;
    }

    public void recycle() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public boolean recycled() {
        return bitmap == null;
    }

    public void set(Bitmap bm) {
        recycle();
        if (bm == null) {
            return;
        }
        bitmap = bm;
    }
}
