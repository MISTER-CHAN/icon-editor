package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Paint;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.Size;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Layer {
    public enum Filter {
        COLOR_MATRIX, CURVES, HSV
    }

    public boolean drawBelow = false;
    public boolean reference = false;
    public boolean visible = true;
    public Bitmap bitmap;
    public final CellGrid cellGrid = new CellGrid();
    public final Deque<Guide> guides = new LinkedList<>();
    public Filter filter;
    public final History history = new History();
    private int level = 0;
    public int left = 0, top = 0;
    public String name;

    @Size(20)
    public float[] colorMatrix;

    @Size(3)
    public float[] deltaHsv;

    public int[][] curves;

    public final Paint paint = new Paint() {
        {
            setAntiAlias(false);
            setFilterBitmap(false);
        }
    };

    public int getLevel() {
        return level;
    }

    public void initColorMatrix() {
        colorMatrix = new float[]{
                1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        };
    }

    public void initCurves() {
        curves = new int[5][];
        for (int i = 0; i <= 4; ++i) {
            curves[i] = new int[0x100];
            for (int j = 0x0; j < 0x100; ++j) {
                curves[i][j] = j;
            }
        }
    }

    public void initDeltaHsv() {
        deltaHsv = new float[]{0.0f, 0.0f, 0.0f};
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

    public void setLevel(@IntRange(from = 0) int level) {
        if (level < 0) {
            return;
        }
        this.level = level;
    }
}
