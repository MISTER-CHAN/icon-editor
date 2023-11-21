package com.misterchan.iconeditor.colorpicker;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorSpace;

import androidx.annotation.ColorLong;
import androidx.annotation.Size;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.util.ColorUtils;

class YuvColorPicker extends ColorPicker {
    private static final float[] RGB_TO_YUV = new ColorMatrix() {
        {
            setRGB2YUV();
        }
    }.getArray();

    private static final float[] YUV_TO_RGB = new ColorMatrix() {
        {
            setYUV2RGB();
        }
    }.getArray();

    private final ColorSpace.Connector connector;

    @Size(3)
    private final float[] yuv = new float[3], rgb = new float[3];

    @ColorLong
    private long color;

    YuvColorPicker(long color) {
        prop = new Properties(true, false, "Y", "U", "V",
                0.0f, 1.0f, -0.5f, 0.5f, -0.5f, 0.5f,
                EDITOR_TYPE_NUM_DEC, EDITOR_TYPE_NUM_DEC_SIGNED, EDITOR_TYPE_NUM_DEC_SIGNED, null);

        connector = ColorSpace.connect(Color.colorSpace(color));

        this.color = color;
        convert(new float[]{Color.red(color), Color.green(color), Color.blue(color)}, yuv, RGB_TO_YUV);
        rgb[0] = Color.red(color);
        rgb[1] = Color.green(color);
        rgb[2] = Color.blue(color);
    }

    @Override
    long color() {
        return color;
    }

    @Override
    int colorInt() {
        return ColorUtils.convert(rgb[0], rgb[1], rgb[2], Color.alpha(color), connector);
    }

    public static void convert(@Size(3) final float[] src, @Size(3) final float[] dst, @Size(20) final float[] matrix) {
        dst[0] = src[0] * matrix[0] + src[1] * matrix[1] + src[2] * matrix[2];
        dst[1] = src[0] * matrix[5] + src[1] * matrix[6] + src[2] * matrix[7];
        dst[2] = src[0] * matrix[10] + src[1] * matrix[11] + src[2] * matrix[12];
    }

    @Override
    float getComponent(int index) {
        return yuv[index];
    }

    @Override
    void setAlpha(float a) {
        color = ColorUtils.setAlpha(color, Settings.INST.colorRep() ? a : a / 0xFF);
    }

    @Override
    void setComponent(int index, float c) {
        yuv[index] = c;
        convert(yuv, rgb, YUV_TO_RGB);
        color = ColorUtils.set(color, index, rgb[index]);
    }
}
