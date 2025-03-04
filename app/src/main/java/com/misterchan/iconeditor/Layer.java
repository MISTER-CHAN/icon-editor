package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.Paint;

import androidx.annotation.IntRange;
import androidx.annotation.Size;

import com.misterchan.iconeditor.dialog.CurvesAdjuster;

import java.util.Arrays;

public class Layer {
    public enum Filter {
        COLOR_BALANCE,
        COLOR_MATRIX,
        CONTRAST,
        CURVES,
        HS,
        LEVELS,
        LIGHTING,
        LIGHTNESS,
        SATURATION,
        SELECTED_BY_CR,
        THRESHOLD
    }

    public boolean clipped = false;
    public boolean reference = false;
    public boolean visible = true;
    public Bitmap bitmap;
    public Canvas canvas;
    public ColorMatrix colorMatrix;
    public final ColorRange colorRange = new ColorRange();
    public Filter filter;
    public int left = 0, top = 0;
    public int[][] curves;
    public String name;

    @Size(4)
    public float[] deltaHs;

    @IntRange(from = 0x0, to = 0x400)
    private int level = 0;

    @Size(8)
    public float[] lighting;

    /**
     * <table>
     *     <tr><th>Operator &nbsp;</th><th>Bits</th></tr>
     *     <tr><td>Root &nbsp;</td><td>10</td></tr>
     *     <tr><td>Foreground Leaf &nbsp;</td><td>10</td></tr>
     *     <tr><td>Parent Background &nbsp;</td><td>10</td></tr>
     *     <tr><td>Lower Level &nbsp;</td><td>1</td></tr>
     * </table>
     */
    public int displayingOperators;

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
        clipped = layer.clipped;
        reference = layer.reference;
        visible = layer.visible;
        this.bitmap = bitmap;
        canvas = new Canvas(bitmap);
        filter = layer.filter;
        level = layer.level;
        left = layer.left;
        top = layer.top;
        this.name = name;
        if (layer.colorMatrix != null) colorMatrix.set(layer.colorMatrix);
        if (layer.colorRange.enabled) colorRange.set(layer.colorRange);
        if (layer.deltaHs != null) deltaHs = Arrays.copyOf(layer.deltaHs, 4);
        if (layer.lighting != null) lighting = Arrays.copyOf(layer.lighting, 8);
        if (layer.curves != null) curves = CurvesAdjuster.copyOf(layer.curves);
        paint = new Paint(layer.paint);
    }

    public int getLevel() {
        return level;
    }

    public void initColorMatrix() {
        colorMatrix = new ColorMatrix();
    }

    public void initCurves() {
        curves = CurvesAdjuster.newCurves();
    }

    public void initDeltaHs() {
        deltaHs = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
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

    public void setLevel(@IntRange(from = 0x0, to = 0x400) int level) {
        if (level < 0) {
            return;
        }
        this.level = level;
    }
}
