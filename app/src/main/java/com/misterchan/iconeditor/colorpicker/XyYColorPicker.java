package com.misterchan.iconeditor.colorpicker;

import android.graphics.Color;
import android.graphics.ColorSpace;

import androidx.annotation.ColorLong;
import androidx.annotation.Size;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.util.ColorUtils;

public class XyYColorPicker extends ColorPicker {
    private final ColorSpace.Connector connectorFromXyz, connectorToXyz, connectorToSrgb;

    @Size(3)
    private final float[] xyY = new float[3];

    @ColorLong
    private long color;

    public XyYColorPicker(long color) {
        prop = new Properties("x", "y", "Y",
                0.0f, 0.8f, 0.0f, 0.9f, -2.0f, 2.0f,
                EDITOR_TYPE_NUM_DEC, EDITOR_TYPE_NUM_DEC, EDITOR_TYPE_NUM_DEC_SIGNED);

        ColorSpace colorSpace = Color.colorSpace(color);
        connectorFromXyz = ColorSpace.connect(XyzColorPicker.XYZ, colorSpace);
        connectorToXyz = ColorSpace.connect(colorSpace, XyzColorPicker.XYZ);
        connectorToSrgb = ColorSpace.connect(colorSpace, SRGB);

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
        return xyY[index];
    }

    @Override
    public void setAlpha(float a) {
        color = ColorUtils.setAlpha(color, Settings.INST.colorRep() ? a : a / 0xFF);
    }

    @Override
    public void setComponent(int index, float c) {
        xyY[index] = c;
        final float yoy = xyY[2] / xyY[1], x = yoy * xyY[0], z = yoy * (1.0f - xyY[0] - xyY[1]);
        color = Color.convert(x, xyY[2], z, Color.alpha(color), connectorFromXyz);
    }

    private void setCompsFromRgb(long color) {
        this.color = color;
        float[] xyz = connectorToXyz.transform(Color.red(color), Color.green(color), Color.blue(color));
        final float sum = xyz[0] + xyz[1] + xyz[2];
        if (sum == 0.0f) {
            xyY[0] = xyY[1] = xyY[2] = 0.0f;
        } else {
            xyY[0] = xyz[0] / sum;
            xyY[1] = xyz[1] / sum;
            xyY[2] = xyz[1];
        }
    }
}
