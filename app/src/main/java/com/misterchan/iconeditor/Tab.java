package com.misterchan.iconeditor;

import android.graphics.Bitmap;

class Tab {
    boolean visible;
    Bitmap bitmap;
    Bitmap.CompressFormat compressFormat;
    BitmapHistory history;
    CellGrid cellGrid;
    float scale;
    float translationX, translationY;
    String path;
}
