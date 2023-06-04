package com.misterchan.iconeditor;

import android.graphics.Bitmap;

interface FloatingLayer {
    Bitmap getBitmap();

    int getLeft();

    int getTop();

    default int getWidth() {
        return getBitmap().getWidth();
    }

    default int getHeight() {
        return getBitmap().getHeight();
    }
}
