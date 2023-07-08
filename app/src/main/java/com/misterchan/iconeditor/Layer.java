package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.Paint;

import androidx.annotation.IntRange;
import androidx.annotation.Size;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class Layer {
    public enum Filter {
        COLOR_BALANCE,
        COLOR_MATRIX,
        CONTRAST,
        CURVES,
        HSV,
        LEVELS,
        LIGHTING,
        LIGHTNESS,
        SATURATION,
        THRESHOLD
    }

    public boolean clipToBelow = false;
    public boolean passBelow = false;
    public boolean reference = false;
    public boolean visible = true;
    public Bitmap bitmap;
    public final CellGrid cellGrid = new CellGrid();
    public ColorMatrix colorMatrix;
    public final Deque<Guide> guides = new LinkedList<>();
    public Filter filter;
    public final History history = new History();
    private int level = 0;
    public int left = 0, top = 0;
    public String name;

    @Size(3)
    public float[] deltaHsv;

    @Size(8)
    public float[] lighting;

    public int[][] curves;

    public final Paint paint;

    public Layer() {
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
    }

    public Layer(Layer layer) {
        this(layer, Bitmap.createBitmap(layer.bitmap), layer.name);
    }

    public Layer(Layer layer, Bitmap bitmap, String name) {
        passBelow = layer.passBelow;
        reference = layer.reference;
        visible = layer.visible;
        this.bitmap = bitmap;
        filter = layer.filter;
        level = layer.level;
        left = layer.left;
        top = layer.top;
        this.name = name;
        if (layer.colorMatrix != null) colorMatrix.set(layer.colorMatrix);
        if (layer.deltaHsv != null) deltaHsv = Arrays.copyOf(layer.deltaHsv, 3);
        if (layer.curves != null) curves = Arrays.copyOf(layer.curves, 5);
        paint = new Paint(layer.paint);
    }

    public int getLevel() {
        return level;
    }

    public void initColorMatrix() {
        colorMatrix = new ColorMatrix();
    }

    public void initCurves() {
        curves = new int[5][0x100];
        for (int i = 0; i <= 4; ++i)
            for (int j = 0x0; j < 0x100; ++j)
                curves[i][j] = j;
    }

    public void initDeltaHsv() {
        deltaHsv = new float[]{0.0f, 0.0f, 0.0f};
    }

    public void initLighting() {
        lighting = new float[]{1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f};
    }

    public void levelDown() {
        ++level;
    }

    public void levelUp() {
        if (level <= 0) {
            return;
        }
        --level;
    }

    public void moveBy(int dx, int dy) {
        left += dx;
        top += dy;
    }

    public void moveTo(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public void resetLighting() {
        lighting[0] = lighting[2] = lighting[4] = lighting[6] = 1.0f;
        lighting[1] = lighting[3] = lighting[5] = lighting[7] = 0.0f;
    }

    public void setLevel(@IntRange(from = 0) int level) {
        if (level < 0) {
            return;
        }
        this.level = level;
    }
}
