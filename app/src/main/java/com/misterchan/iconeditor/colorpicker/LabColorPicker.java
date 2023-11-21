package com.misterchan.iconeditor.colorpicker;

import android.graphics.Color;
import android.graphics.ColorSpace;

import androidx.annotation.ColorLong;
import androidx.annotation.Size;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.util.ColorUtils;

class LabColorPicker extends ColorPicker {
    private static final ColorSpace LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);

    private final ColorSpace.Connector connectorFromLab, connectorToSrgb;

    @Size(3)
    private final float[] lab;

    @ColorLong
    private long color;

    LabColorPicker(long color) {
        prop = new Properties(true, false, "L*", "a*", "b*",
                0.0f, 100.0f, -128.0f, 128.0f, -128.0f, 128.0f,
                EDITOR_TYPE_NUM_DEC, EDITOR_TYPE_NUM_DEC_SIGNED, EDITOR_TYPE_NUM_DEC_SIGNED, null);

        ColorSpace colorSpace = Color.colorSpace(color);
        connectorFromLab = ColorSpace.connect(LAB, colorSpace);
        ColorSpace.Connector connectorToLab = ColorSpace.connect(colorSpace, LAB);
        connectorToSrgb = ColorSpace.connect(colorSpace, SRGB);

        this.color = color;
        lab = connectorToLab.transform(Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    long color() {
        return color;
    }

    @Override
    int colorInt() {
        return ColorUtils.convert(color, connectorToSrgb);
    }

    @Override
    float getComponent(int index) {
        return lab[index];
    }

    @Override
    void setAlpha(float a) {
        color = ColorUtils.setAlpha(color, Settings.INST.colorRep() ? a : a / 0xFF);
    }

    @Override
    void setComponent(int index, float c) {
        lab[index] = c;
        color = Color.convert(lab[0], lab[1], lab[2], Color.alpha(color), connectorFromLab);
    }
}
