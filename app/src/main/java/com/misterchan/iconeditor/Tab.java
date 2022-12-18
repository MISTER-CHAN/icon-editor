package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Size;

class Tab {
    public boolean colorFilterEnabled = false;
    public boolean HSVEnabled = false;
    public boolean drawBelow = false;
    public boolean visible;
    public Bitmap bitmap;
    public Bitmap.CompressFormat compressFormat;
    public BitmapHistory history;
    public CellGrid cellGrid;
    public CheckBox cbLayerVisible;
    public float scale;
    public float translationX, translationY;
    public int level = 0;
    public Paint paint;
    public String path;
    public TextView tvLayerLevel;
    public TextView tvTitle;

    @Size(20)
    public float[] colorMatrix = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    @Size(3)
    public float[] deltaHSV = new float[]{0.0f, 0.0f, 0.0f};
}
