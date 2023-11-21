package com.misterchan.iconeditor.colorpicker;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.util.ColorUtils;

class CmykColorPicker extends ColorPicker {
    @Size(4)
    private final int[] cmyk = new int[4];

    @ColorInt
    private int colorInt;

    CmykColorPicker(long color) {
        prop = new Properties(false, true, "C", "M", "Y",
                0x00, 0xFF, 0x00, 0xFF, 0x00, 0xFF,
                EDITOR_TYPE_NUM, EDITOR_TYPE_NUM, EDITOR_TYPE_NUM, KEY_LISTENER_HEX);

        setCompsFromRgb(color);
    }

    @Override
    long color() {
        return Color.pack(colorInt);
    }

    @Override
    int colorInt() {
        return colorInt;
    }

    @Override
    float getComponent(int index) {
        return cmyk[index];
    }

    @Override
    void setAlpha(float alpha) {
        int a = (int) (Settings.INST.colorRep() ? alpha * 0xFF : alpha);
        colorInt = ColorUtils.setAlpha(colorInt, a);
    }

    @Override
    void setComponent(int index, float comp) {
        cmyk[index] = (int) comp;
        int c = cmyk[0], m = cmyk[1], y = cmyk[2], k = cmyk[3];
        int invK = 0xFF - k;
        int r = 0xFF - c * invK / 0xFF - k, g = 0xFF - m * invK / 0xFF - k, b = 0xFF - y * invK / 0xFF - k;
        colorInt = ColorUtils.clipped(colorInt, ColorUtils.rgb(r, g, b));
    }

    private void setCompsFromRgb(long color) {
        colorInt = Color.toArgb(color);
        final int r = Color.red(colorInt), g = Color.green(colorInt), b = Color.blue(colorInt);
        final int c_ = 0xFF - r, m_ = 0xFF - g, y_ = 0xFF - b;
        final int k = Math.min(Math.min(c_, m_), y_), invK = 0xFF - k;
        cmyk[0] = k == 0xFF ? 0x00 : (c_ - k) * 0xFF / invK;
        cmyk[1] = k == 0xFF ? 0x00 : (m_ - k) * 0xFF / invK;
        cmyk[2] = k == 0xFF ? 0x00 : (y_ - k) * 0xFF / invK;
        cmyk[3] = k;
    }
}
