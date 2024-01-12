package com.misterchan.iconeditor.util;

import android.graphics.Color;
import android.graphics.ColorSpace;
import android.util.Half;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Size;

public class ColorUtils {
    private ColorUtils() {
    }

    @ColorInt
    public static int argb(@IntRange(from = 0x00, to = 0xFF) int alpha, @ColorInt int rgb) {
        return alpha << 24 | rgb;
    }

    @IntRange(from = 0x00, to = 0xFF)
    public static int brightness(@ColorInt int color) {
        return Math.max(Math.max(Color.red(color), Color.green(color)), Color.blue(color));
    }

    @ColorInt
    public static int clipped(@ColorInt int dst, @ColorInt int src) {
        return dst & Color.BLACK | src & 0x00FFFFFF;
    }

    @ColorInt
    public static int clipped(@ColorInt int dst,
                              @IntRange(from = 0x00, to = 0xFF) int red,
                              @IntRange(from = 0x00, to = 0xFF) int green,
                              @IntRange(from = 0x00, to = 0xFF) int blue) {
        return dst & Color.BLACK | rgb(red, green, blue);
    }

    @Size(3)
    public static void colorToHSL(@ColorInt int color, @Size(min = 3) float[] hsl) {
        final float r = Color.red(color) / 255.0f, g = Color.green(color) / 255.0f, b = Color.blue(color) / 255.0f;
        final float max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        hsl[2] = (max + min) / 2.0f;
        if (max == min) {
            hsl[0] = hsl[1] = 0.0f;
        } else {
            if (max == r) {
                hsl[0] = 60.0f * (g - b) / (max - min) + (g >= b ? 0.0f : 360.0f);
            } else if (max == g) {
                hsl[0] = 60.0f * (b - r) / (max - min) + 120.0f;
            } else if (max == b) {
                hsl[0] = 60.0f * (r - g) / (max - min) + 240.0f;
            }
            hsl[1] = sat((max - min) / (1.0f - Math.abs(2.0f * hsl[2] - 1.0f)));
        }
    }

    /**
     * Override the method for a faster conversion.
     */
    @Size(3)
    public static void colorToHSV(@ColorInt int color, @Size(min = 3) float[] hsv) {
        final float r = Color.red(color) / 255.0f, g = Color.green(color) / 255.0f, b = Color.blue(color) / 255.0f;
        final float max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        if (max == min) {
            hsv[0] = hsv[1] = 0.0f;
        } else {
            if (max == r) {
                hsv[0] = 60.0f * (g - b) / (max - min) + (g >= b ? 0.0f : 360.0f);
            } else if (max == g) {
                hsv[0] = 60.0f * (b - r) / (max - min) + 120.0f;
            } else if (max == b) {
                hsv[0] = 60.0f * (r - g) / (max - min) + 240.0f;
            }
            hsv[1] = 1.0f - min / max;
        }
        hsv[2] = max;
    }

    /**
     * Convert to sRGB with an existing connector. This is a little bit more efficient than {@link Color#toArgb(long)}.
     */
    @ColorInt
    public static int convert(long color, ColorSpace.Connector connector) {
        return convert(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color), connector);
    }

    @ColorInt
    public static int convert(float r, float g, float b, float a, ColorSpace.Connector connector) {
        // The transformation saturates the output
        float[] c = connector.transform(r, g, b);

        return (int) (a * 255.0f + 0.5f) << 24
                | (int) (c[0] * 255.0f + 0.5f) << 16 | (int) (c[1] * 255.0f + 0.5f) << 8 | (int) (c[2] * 255.0f + 0.5f);
    }

    public static double distance(@ColorInt int color1, @ColorInt int color2) {
        final int dr = Color.red(color1) - Color.red(color2), dg = Color.green(color1) - Color.green(color2), db = Color.blue(color1) - Color.blue(color2);
        return dr * dr + dg * dg + db * db;
    }

    @FloatRange(from = 0.0f, to = 360.0f, toInclusive = false)
    public static float hue(@ColorInt int color) {
        final int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        final int max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        if (max == min) {
            return 0.0f;
        } else if (max == r) {
            return 60.0f * (g - b) / (max - min) + (g >= b ? 0.0f : 360.0f);
        } else if (max == g) {
            return 60.0f * (b - r) / (max - min) + 120.0f;
        } else /* if (max == b) */ {
            return 60.0f * (r - g) / (max - min) + 240.0f;
        }
    }

    @ColorInt
    public static int HSLToColor(@Size(min = 3) float[] hsl) {
        final float h = hsl[0], s = hsl[1], l = hsl[2];
        final float c = (1.0f - Math.abs(2.0f * l - 1.0f)) * s;
        final float h_ = h / 60.0f;
        final float x = c * (1.0f - Math.abs(h_ % 2.0f - 1.0f));
        @ColorInt final int color = switch ((int) h_) {
            case 0 -> Color.argb(0.0f, c, x, 0.0f);
            case 1 -> Color.argb(0.0f, x, c, 0.0f);
            case 2 -> Color.argb(0.0f, 0.0f, c, x);
            case 3 -> Color.argb(0.0f, 0.0f, x, c);
            case 4 -> Color.argb(0.0f, x, 0.0f, c);
            case 5 -> Color.argb(0.0f, c, 0.0f, x);
            default -> Color.TRANSPARENT;
        };
        final float m = l - 0.5f * c;
        return color + (int) (m * 0xFF) * 0x010101;
    }

    /**
     * Override the method for a faster conversion.
     */
    @ColorInt
    public static int HSVToColor(@Size(min = 3) float[] hsv) {
        final float h = hsv[0], s = hsv[1], v = hsv[2];
        final int hi = (int) (h / 60.0f);
        final float f = h / 60.0f - hi;
        final float p = sat(v * (1.0f - s));
        final float q = sat(v * (1.0f - f * s));
        final float t = sat(v * (1.0f - (1.0f - f) * s));
        return switch (hi) {
            case 0 -> Color.argb(0.0f, v, t, p);
            case 1 -> Color.argb(0.0f, q, v, p);
            case 2 -> Color.argb(0.0f, p, v, t);
            case 3 -> Color.argb(0.0f, p, q, v);
            case 4 -> Color.argb(0.0f, t, p, v);
            case 5 -> Color.argb(0.0f, v, p, q);
            default -> Color.TRANSPARENT;
        };
    }

    public static boolean matches(@ColorInt int c0, @ColorInt int color, int tolerance) {
        return Math.abs(Color.red(color) - Color.red(c0)) <= tolerance
                && Math.abs(Color.green(color) - Color.green(c0)) <= tolerance
                && Math.abs(Color.blue(color) - Color.blue(c0)) <= tolerance;
    }

    @ColorInt
    public static int rgb(@ColorInt int color) {
        return color & 0x00FFFFFF;
    }

    @ColorInt
    public static int rgb(@IntRange(from = 0x00, to = 0xFF) int red,
                          @IntRange(from = 0x00, to = 0xFF) int green,
                          @IntRange(from = 0x00, to = 0xFF) int blue) {
        return red << 16 | green << 8 | blue;
    }

    /**
     * <b>Saturate</b> (a.k.a. <b>clamp</b>, <b>constrain</b>)
     */
    @FloatRange(from = 0.0f, to = 1.0f)
    public static float sat(float c) {
        return c <= 0.0f ? 0.0f : c >= 1.0f ? 1.0f : c;
    }

    /**
     * <b>Saturate</b> (a.k.a. <b>clamp</b>, <b>constrain</b>)
     */
    @IntRange(from = 0x00, to = 0xFF)
    public static int sat(int c) {
        return c <= 0x00 ? 0x00 : c >= 0xFF ? 0xFF : c;
    }

    /**
     * @param index The index of <code>{</code>R<code>, </code>G<code>, </code>B<code>}</code>
     */
    @ColorInt
    public static int setComponent(@ColorInt int color, @IntRange(from = 0, to = 2) int index, @IntRange(from = 0x00, to = 0xFF) int comp) {
        return comp << ((2 - index) << 3) | color & ~(0xFF << ((2 - index) << 3));
    }

    @ColorInt
    public static int setAlpha(@ColorInt int color, @IntRange(from = 0x00, to = 0xFF) int alpha) {
        return alpha << 24 | color & 0x00FFFFFF;
    }

    /**
     * @param index The index of <code>{</code>R<code>, </code>G<code>, </code>B<code>}</code>
     */
    @ColorLong
    public static long setComponent(@ColorLong long color, @IntRange(from = 0, to = 2) int index, float comp) {
        ColorSpace colorSpace = Color.colorSpace(color);
        if (colorSpace.isSrgb()) {
            return (long) (comp * 255.0f + 0.5) << ((2 - index) << 3) << 32 | color & ~(0xFFL << ((2 - index) << 3) << 32);
        }

        long c = (short) Half.toHalf(comp) & 0xFFFFL;
        return c << ((3 - index) << 4) | color & ~(0xFFFFL << ((3 - index) << 4));
    }

    @ColorLong
    public static long setAlpha(@ColorLong long color, @FloatRange(from = 0.0f, to = 1.0f) float alpha) {
        ColorSpace colorSpace = Color.colorSpace(color);
        if (colorSpace.isSrgb()) {
            return (long) (alpha * 255.0f + 0.5f) << 56 | color & 0x00FFFFFFFFFFFFFFL;
        }

        long a = (int) (Math.max(0.0f, Math.min(alpha, 1.0f)) * 1023.0f + 0.5f) & 0x3FFL;
        return a << 6 | color & 0xFFFFFFFFFFFF003FL;
    }

    @ColorLong
    public static long setAlpha(@ColorLong long color, @IntRange(from = 0x00, to = 0xFF) int alpha) {
        ColorSpace colorSpace = Color.colorSpace(color);
        if (colorSpace.isSrgb()) {
            return (long) alpha << 56 | color & 0x00FFFFFFFFFFFFFFL;
        }

        long a = (int) (Math.max(0.0f, Math.min(alpha / 255.0f, 1.0f)) * 1023.0f + 0.5f) & 0x3FFL;
        return a << 6 | color & 0xFFFFFFFFFFFF003FL;
    }
}
