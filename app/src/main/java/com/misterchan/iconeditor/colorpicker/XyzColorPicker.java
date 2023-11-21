package com.misterchan.iconeditor.colorpicker;

import android.graphics.Color;
import android.graphics.ColorSpace;

import androidx.annotation.ColorLong;
import androidx.annotation.Size;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.util.ColorUtils;

class XyzColorPicker extends ColorPicker {
    static final ColorSpace XYZ = ColorSpace.get(ColorSpace.Named.CIE_XYZ);

    private final ColorSpace.Connector connectorFromXyz, connectorToSrgb;

    @Size(3)
    private final float[] xyz;

    @ColorLong
    private long color;

    XyzColorPicker(long color) {
        prop = new Properties(true, false, "X", "Y", "Z",
                -2.0f, 2.0f, -2.0f, 2.0f, -2.0f, 2.0f,
                EDITOR_TYPE_NUM_DEC_SIGNED, EDITOR_TYPE_NUM_DEC_SIGNED, EDITOR_TYPE_NUM_DEC_SIGNED, null);

        ColorSpace colorSpace = Color.colorSpace(color);
        connectorFromXyz = ColorSpace.connect(XYZ, colorSpace);
        ColorSpace.Connector connectorToXyz = ColorSpace.connect(colorSpace, XYZ);
        connectorToSrgb = ColorSpace.connect(colorSpace, SRGB);

        this.color = color;
        xyz = connectorToXyz.transform(Color.red(color), Color.green(color), Color.blue(color));
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
        return xyz[index];
    }

    @Override
    void setAlpha(float a) {
        color = ColorUtils.setAlpha(color, Settings.INST.colorRep() ? a : a / 0xFF);
    }

    @Override
    void setComponent(int index, float c) {
        xyz[index] = c;
        color = Color.convert(xyz[0], xyz[1], xyz[2], Color.alpha(color), connectorFromXyz);
    }
}
