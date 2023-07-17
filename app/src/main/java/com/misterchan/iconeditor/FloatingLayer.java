package com.misterchan.iconeditor;

import android.graphics.Bitmap;

interface FloatingLayer {
    default boolean hasRect() {
        return false;
    }

    Bitmap getBitmap();

    default int getLeft() {
        return 0;
    }

    default int getTop() {
        return 0;
    }

    default int getWidth() {
        return getBitmap().getWidth();
    }

    default int getHeight() {
        return getBitmap().getHeight();
    }
}
