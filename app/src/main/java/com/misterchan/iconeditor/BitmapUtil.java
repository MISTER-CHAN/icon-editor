package com.misterchan.iconeditor;

import android.graphics.Bitmap;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

import java.util.Random;

public class BitmapUtil {
    public static void addColorFilter(@ColorInt final int[] src, @ColorInt final int[] dst,
                                       final int area, final float scale, final float shift) {
        for (int i = 0; i < area; ++i) {
            final int r = Color.red(src[i]), g = Color.green(src[i]), b = Color.blue(src[i]),
                    a = Color.alpha(src[i]);
            final int r_ = Color.saturate((int) (r * scale + shift));
            final int g_ = Color.saturate((int) (g * scale + shift));
            final int b_ = Color.saturate((int) (b * scale + shift));
            dst[i] = Color.argb(a, r_, g_, b_);
        }
    }

    public static void addColorFilter(final Bitmap src, final int srcX, final int srcY,
                                      final Bitmap dst, final int dstX, final int dstY,
                                      final float scale, final float shift) {
        final int w = src.getWidth(), h = src.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        src.getPixels(pixels, 0, w, srcX, srcY, w, h);
        addColorFilter(pixels, pixels, area, scale, shift);
        dst.setPixels(pixels, 0, w, dstX, dstY, w, h);
    }

    public static void addColorFilter(@ColorInt final int[] src, @ColorInt final int[] dst,
                                       final int area, @Size(20) final float[] colorMatrix) {
        for (int i = 0; i < area; ++i) {
            final int r = Color.red(src[i]), g = Color.green(src[i]), b = Color.blue(src[i]),
                    a = Color.alpha(src[i]);
            final int r_ = Color.saturate((int) (r * colorMatrix[0] + g * colorMatrix[1] + b * colorMatrix[2] + a * colorMatrix[3] + colorMatrix[4]));
            final int g_ = Color.saturate((int) (r * colorMatrix[5] + g * colorMatrix[6] + b * colorMatrix[7] + a * colorMatrix[8] + colorMatrix[9]));
            final int b_ = Color.saturate((int) (r * colorMatrix[10] + g * colorMatrix[11] + b * colorMatrix[12] + a * colorMatrix[13] + colorMatrix[14]));
            final int a_ = Color.saturate((int) (r * colorMatrix[15] + g * colorMatrix[16] + b * colorMatrix[17] + a * colorMatrix[18] + colorMatrix[19]));
            dst[i] = Color.argb(a_, r_, g_, b_);
        }
    }

    public static void addColorFilter(final Bitmap src, final int srcX, final int srcY,
                                      final Bitmap dst, final int dstX, final int dstY,
                                      @Size(20) final float[] colorMatrix) {
        final int w = src.getWidth(), h = src.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        src.getPixels(pixels, 0, w, srcX, srcY, w, h);
        addColorFilter(pixels, pixels, area, colorMatrix);
        dst.setPixels(pixels, 0, w, dstX, dstY, w, h);
    }

    private static Bitmap edgeDetection(final Bitmap bitmap) {
        return null;
    }

    public static void generateNoise(@ColorInt final int[] pixels, final int area, @ColorInt final int color,
                                     final float noisy, final Long seed) {
        final Random random = seed == null ? new Random() : new Random(seed);
        for (int i = 0; i < area; ++i) {
            if (random.nextFloat() < noisy) {
                pixels[i] = color;
            }
        }
    }

    /**
     * @param fc Foreground color
     * @param bc Background color
     */
    public static void removeBackground(final Bitmap bitmap, @ColorInt final int fc, @ColorInt final int bc) {
        final float fr = Color.red(fc) / 255.0f, fg = Color.green(fc) / 255.0f, fb = Color.blue(fc) / 255.0f, fa = Color.alpha(fc) / 255.0f;
        final float br = Color.red(bc) / 255.0f, bg = Color.green(bc) / 255.0f, bb = Color.blue(bc) / 255.0f;
        final float dr = fr - br, dg = fg - bg, db = fb - bb, sd = dr + dg + db; // Differences
        final float rr = dr / sd, rg = dg / sd, rb = db / sd; // Ratios
        final int w = bitmap.getWidth(), h = bitmap.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            // Color
            final float r = Color.red(pixels[i]) / 255.0f,
                    g = Color.green(pixels[i]) / 255.0f,
                    b = Color.blue(pixels[i]) / 255.0f;

            /*
             * c = a * f + (1 - a) * b => a = (c - 1 * b) / (f - b)
             * Where c - Output color value
             *       a - Foreground alpha value
             *       f - Foreground color value
             *       b - Background color value
             */
            final float a_ = (dr == 0.0f ? 0.0f : (r - fa * br) / dr * rr)
                    + (dg == 0.0f ? 0.0f : (g - fa * bg) / dg * rg)
                    + (db == 0.0f ? 0.0f : (b - fa * bb) / db * rb);

            pixels[i] = Color.argb(Color.saturate(a_), fr, fg, fb);
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    public static void shiftHSV(@ColorInt final int[] src, @ColorInt final int[] dst,
                                final int area, @Size(3) final float[] deltaHSV) {
        final float[] hsv = new float[3];
        for (int i = 0; i < area; ++i) {
            final int pixel = src[i];
            Color.colorToHSV(pixel, hsv);
            hsv[0] = (hsv[0] + deltaHSV[0] + 360.0f) % 360.0f;
            hsv[1] = Color.saturate(hsv[1] + deltaHSV[1]);
            hsv[2] = Color.saturate(hsv[2] + deltaHSV[2]);
            dst[i] = pixel & Color.BLACK | Color.HSVToColor(hsv);
        }
    }

    public static void shiftHSV(final Bitmap bitmap, @Size(3) final float[] deltaHSV) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        shiftHSV(pixels, pixels, area, deltaHSV);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }
}
