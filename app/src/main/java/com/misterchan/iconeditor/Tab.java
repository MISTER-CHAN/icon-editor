package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.widget.CheckBox;
import android.widget.TextView;

class Tab {
    public boolean sub;
    public boolean visible;
    public Bitmap bitmap;
    public Bitmap.CompressFormat compressFormat;
    public BitmapHistory history;
    public CellGrid cellGrid;
    public CheckBox cbLayerVisible;
    public float scale;
    public float translationX, translationY;
    public Paint paint;
    public String path;
    public TextView tvSub;
    public TextView tvTitle;
}
