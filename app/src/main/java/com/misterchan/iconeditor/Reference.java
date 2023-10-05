package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Rect;

class Reference {
    private Bitmap bitmap;
    private final Rect rect = new Rect();

    public Bitmap bm() {
        return bitmap;
    }

    public boolean recycled() {
        return bitmap == null;
    }

    public Rect rect() {
        return rect;
    }

    public void recycle() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void set(Bitmap bm) {
        recycle();
        if (bm == null) {
            return;
        }
        bitmap = bm;
        rect.right = bm.getWidth();
        rect.bottom = bm.getHeight();
    }
}
