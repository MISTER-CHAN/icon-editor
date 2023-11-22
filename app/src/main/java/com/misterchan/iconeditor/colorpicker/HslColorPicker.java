package com.misterchan.iconeditor.colorpicker;

import android.annotation.SuppressLint;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.Size;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.util.ColorUtils;

public class HslColorPicker extends ColorPicker {
    @Size(3)
    private final float[] hsl = new float[3];

    @ColorInt
    private int colorInt;

    @SuppressLint("ClickableViewAccessibility")
    public HslColorPicker(long color) {
        prop = new Properties("H", "S", "L", true);

        setColorFromRgb(color);
    }

    @Override
    public long color() {
        return Color.pack(colorInt);
    }

    @Override
    public int colorInt() {
        return colorInt;
    }

    @Override
    public float getComponent(int index) {
        return index == 0 ? hsl[0] : hsl[index] * 100.0f;
    }

    @Override
    public void setAlpha(float a) {
        colorInt = ColorUtils.setAlpha(colorInt, (int) (Settings.INST.colorRep() ? a * 0xFF : a));
    }

    @Override
    public void setComponent(@IntRange(from = 0, to = 2) int index, float c) {
        if (index == 1 || index == 2) c /= 100.0f;
        if (hsl[index] == c) return;
        hsl[index] = c;
        colorInt = colorInt & Color.BLACK | ColorUtils.HSLToColor(hsl);
    }

    private void setColorFromRgb(long color) {
        colorInt = Color.toArgb(color);
        ColorUtils.colorToHSL(colorInt, hsl);
    }
}
