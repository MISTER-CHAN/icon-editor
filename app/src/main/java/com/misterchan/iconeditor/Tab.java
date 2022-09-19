package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Paint;

class Tab {
    boolean visible;
    Bitmap bitmap;
    Bitmap.CompressFormat compressFormat;
    BitmapHistory history;
    CellGrid cellGrid;
    float scale;
    float translationX, translationY;
    Paint paint;
    String path;
}
