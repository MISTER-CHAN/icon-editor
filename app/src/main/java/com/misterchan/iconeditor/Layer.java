package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Size;

import java.util.Deque;
import java.util.LinkedList;

public class Layer {
    public boolean drawBelow = false;
    public boolean reference = false;
    public boolean visible = true;
    public Bitmap bitmap;
    public final CellGrid cellGrid = new CellGrid();
    public CharSequence name;
    private CheckBox cbVisible;
    public final Deque<Guide> guides = new LinkedList<>();
    public float scale;
    public float translationX, translationY;
    public final History history = new History();
    private int level = 0;
    public int left = 0, top = 0;
    private TextView tvBackground;
    private TextView tvFrameIndex;
    private TextView tvLowerLevel, tvRoot, tvParent, tvLeaf;
    private TextView tvTitle;

    @Size(20)
    public float[] colorMatrix;

    @Size(3)
    public float[] deltaHsv;

    public int[][] curves;
}
