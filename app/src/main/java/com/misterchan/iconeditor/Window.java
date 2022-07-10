package com.misterchan.iconeditor;

import android.graphics.Bitmap;

class Window {
    Bitmap bitmap;
    Bitmap.CompressFormat compressFormat;
    BitmapHistory history;
    CellGrid cellGrid;
    float scale;
    float translationX, translationY;
    String path;
}
