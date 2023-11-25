package com.misterchan.iconeditor.colorpicker;

import android.graphics.Color;
import android.graphics.ColorSpace;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.Size;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.util.ColorUtils;

public class CmykColorPicker extends ColorPicker {
    private final ColorSpace colorSpace;
    private final ColorSpace.Connector connectorToSrgb;

    @Size(4)
    private final float[] cmyk = new float[4];

    @ColorLong
    private long color;

    public CmykColorPicker(long color) {
        prop = new Properties(true, "C", "M", "Y");

        colorSpace = Color.colorSpace(color);
        connectorToSrgb = ColorSpace.connect(colorSpace);

        setCompsFromRgb(color);
    }

    @Override
    public long color() {
        return color;
    }

    @Override
    public int colorInt() {
        return ColorUtils.convert(color, connectorToSrgb);
    }

    @Override
    public float getComponent(int index) {
        return cmyk[index] * 100.0f;
    }

    @Override
    public void setAlpha(float alpha) {
        color = ColorUtils.setAlpha(color, Settings.INST.colorRep() ? alpha : alpha / 0xFF);
    }

    @Override
    public void setComponent(int index, float comp) {
        cmyk[index] = comp / 100.0f;
        float c = cmyk[0], m = cmyk[1], y = cmyk[2], k = cmyk[3];
        float invK = 1.0f - k;
        float r = 1.0f - c * invK - k, g = 1.0f - m * invK - k, b = 1.0f - y * invK - k;
        color = Color.pack(r, g, b, Color.alpha(color), colorSpace);
    }

    private void setCompsFromRgb(long color) {
        this.color = color;
        final float r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        final float c_ = 1.0f - r, m_ = 1.0f - g, y_ = 1.0f - b;
        final float k = Math.min(Math.min(c_, m_), y_), invK = 1.0f - k;
        cmyk[0] = k == 1.0f ? 0.0f : (c_ - k) / invK;
        cmyk[1] = k == 1.0f ? 0.0f : (m_ - k) / invK;
        cmyk[2] = k == 1.0f ? 0.0f : (y_ - k) / invK;
        cmyk[3] = k;
    }
}
