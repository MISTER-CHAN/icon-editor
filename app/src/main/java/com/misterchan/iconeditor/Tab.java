package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.widget.CheckBox;
import android.widget.TextView;

class Tab {
    boolean sub;
    boolean visible;
    Bitmap bitmap;
    Bitmap.CompressFormat compressFormat;
    BitmapHistory history;
    CellGrid cellGrid;
    CheckBox cbLayerVisible;
    float scale;
    float translationX, translationY;
    Paint paint;
    String path;
    TextView tvSub;
    TextView tvTitle;
}
