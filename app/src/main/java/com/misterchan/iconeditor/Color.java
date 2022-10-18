package com.misterchan.iconeditor;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.Size;

public class Color {

    @Size(3)
    public static float[] colorToHSV(@ColorInt int color) {
        final float r = android.graphics.Color.red(color) / 255.0f,
                g = android.graphics.Color.green(color) / 255.0f,
                b = android.graphics.Color.blue(color) / 255.0f;
        final float max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        float h = 0.0f, s, v;
        if (max == min) {
            h = 0.0f;
        } else if (max == r) {
            h = 60.0f * (g - b) / (max - min) + (g >= b ? 0.0f : 360.0f);
        } else if (max == g) {
            h = 60.0f * (b - r) / (max - min) + 120.0f;
        } else if (max == b) {
            h = 60.0f * (r - g) / (max - min) + 240.0f;
        }
        s = max == 0.0f ? 0.0f : 1.0f - min / max;
        v = max;
        return new float[]{h, s, v};
    }

    @ColorInt
    public static int HSVToColor(@Size(3) float[] hsv) {
        float h = hsv[0], s = hsv[1], v = hsv[2];
        int hi = (int) (h / 60.0f);
        float f = h / 60.0f - hi;
        float p = saturate(v * (1.0f - s));
        float q = saturate(v * (1.0f - f * s));
        float t = saturate(v * (1.0f - (1.0f - f) * s));
        v = saturate(v);
        switch (hi) {
            case 0:
                return android.graphics.Color.argb(0.0f, v, t, p);
            case 1:
                return android.graphics.Color.argb(0.0f, q, v, p);
            case 2:
                return android.graphics.Color.argb(0.0f, p, v, t);
            case 3:
                return android.graphics.Color.argb(0.0f, p, q, v);
            case 4:
                return android.graphics.Color.argb(0.0f, t, p, v);
            case 5:
                return android.graphics.Color.argb(0.0f, v, p, q);
        }
        return 0x00000000;
    }

    private static float saturate(float v) {
        return v <= 0.0f ? 0.0f : v >= 1.0f ? 1.0f : v;
    }
}
