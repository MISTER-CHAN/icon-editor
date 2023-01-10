package com.misterchan.iconeditor;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.Size;

public class Color extends android.graphics.Color {

    @Size(3)
    public static void colorToHSV(@ColorInt int color, @Size(3) float[] hsv) {
        final float r = red(color) / 255.0f, g = green(color) / 255.0f, b = blue(color) / 255.0f;
        final float max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        if (max == min) {
            hsv[0] = 0.0f;
        } else if (max == r) {
            hsv[0] = 60.0f * (g - b) / (max - min) + (g >= b ? 0.0f : 360.0f);
        } else if (max == g) {
            hsv[0] = 60.0f * (b - r) / (max - min) + 120.0f;
        } else if (max == b) {
            hsv[0] = 60.0f * (r - g) / (max - min) + 240.0f;
        }
        hsv[1] = max == 0.0f ? 0.0f : 1.0f - min / max;
        hsv[2] = max;
    }

    public static float hue(@ColorInt int color) {
        final float r = red(color), g = green(color), b = blue(color);
        final float max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        if (max == min) {
            return 0.0f;
        } else if (max == r) {
            return 60.0f * (g - b) / (max - min) + (g >= b ? 0.0f : 360.0f);
        } else if (max == g) {
            return 60.0f * (b - r) / (max - min) + 120.0f;
        } else if (max == b) {
            return 60.0f * (r - g) / (max - min) + 240.0f;
        }
        return 0.0f;
    }

    @ColorInt
    public static int HSVToColor(@Size(3) float[] hsv) {
        float h = hsv[0], s = hsv[1], v = hsv[2];
        final int hi = (int) (h / 60.0f);
        final float f = h / 60.0f - hi;
        final float p = sat(v * (1.0f - s));
        final float q = sat(v * (1.0f - f * s));
        final float t = sat(v * (1.0f - (1.0f - f) * s));
        v = sat(v);
        switch (hi) {
            case 0:
                return argb(0.0f, v, t, p);
            case 1:
                return argb(0.0f, q, v, p);
            case 2:
                return argb(0.0f, p, v, t);
            case 3:
                return argb(0.0f, p, q, v);
            case 4:
                return argb(0.0f, t, p, v);
            case 5:
                return argb(0.0f, v, p, q);
        }
        return TRANSPARENT;
    }

    public static float luminance(@ColorInt int color) {
        return 0.2126f * red(color) / 255.0f
                + 0.7152f * green(color) / 255.0f
                + 0.0722f * blue(color) / 255.0f;
    }

    public static int luminosity(@ColorInt int color) {
        return Math.max(Math.max(red(color), green(color)), blue(color));
    }

    @ColorInt
    public static int rgb(@ColorInt int color) {
        return color & 0x00FFFFFF;
    }

    @ColorInt
    public static int rgb(@IntRange(from = 0, to = 255) int red,
                          @IntRange(from = 0, to = 255) int green,
                          @IntRange(from = 0, to = 255) int blue) {
        return red << 16 | green << 8 | blue;
    }

    public static float sat(float v) {
        return v <= 0.0f ? 0.0f : v >= 1.0f ? 1.0f : v;
    }

    public static int sat(int v) {
        return v <= 0x00 ? 0x00 : v >= 0xFF ? 0xFF : v;
    }
}
