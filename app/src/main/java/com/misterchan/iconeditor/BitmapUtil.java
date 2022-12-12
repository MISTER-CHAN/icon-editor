package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

public class BitmapUtil {

    public static void addColorFilter(final Bitmap src, final int srcX, final int srcY,
                                      final Bitmap dst, final int dstX, final int dstY,
                                      final float scale, final float shift) {
        final int w = src.getWidth(), h = src.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        src.getPixels(pixels, 0, w, srcX, srcY, w, h);
        for (int i = 0; i < area; ++i) {
            final int r = Color.red(pixels[i]), g = Color.green(pixels[i]), b = Color.blue(pixels[i]),
                    a = Color.alpha(pixels[i]);
            final int r_ = inRangeFrom0To255((int) (r * scale + shift));
            final int g_ = inRangeFrom0To255((int) (g * scale + shift));
            final int b_ = inRangeFrom0To255((int) (b * scale + shift));
            pixels[i] = Color.argb(a, r_, g_, b_);
        }
        dst.setPixels(pixels, 0, w, dstX, dstY, w, h);
    }

    public static void addColorFilter(final Bitmap src, final int srcX, final int srcY,
                                      final Bitmap dst, final int dstX, final int dstY,
                                      @Size(20) final float[] colorMatrix) {
        final int w = src.getWidth(), h = src.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        src.getPixels(pixels, 0, w, srcX, srcY, w, h);
        for (int i = 0; i < area; ++i) {
            final int r = Color.red(pixels[i]), g = Color.green(pixels[i]), b = Color.blue(pixels[i]),
                    a = Color.alpha(pixels[i]);
            final int r_ = inRangeFrom0To255((int) (r * colorMatrix[0] + g * colorMatrix[1] + b * colorMatrix[2] + a * colorMatrix[3] + colorMatrix[4]));
            final int g_ = inRangeFrom0To255((int) (r * colorMatrix[5] + g * colorMatrix[6] + b * colorMatrix[7] + a * colorMatrix[8] + colorMatrix[9]));
            final int b_ = inRangeFrom0To255((int) (r * colorMatrix[10] + g * colorMatrix[11] + b * colorMatrix[12] + a * colorMatrix[13] + colorMatrix[14]));
            final int a_ = inRangeFrom0To255((int) (r * colorMatrix[15] + g * colorMatrix[16] + b * colorMatrix[17] + a * colorMatrix[18] + colorMatrix[19]));
            pixels[i] = Color.argb(a_, r_, g_, b_);
        }
        dst.setPixels(pixels, 0, w, dstX, dstY, w, h);
    }

    private static int inRangeFrom0To255(int a) {
        return Math.max(Math.min(a, 255), 0);
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
            final float r = Color.red(pixels[i]) / 255.0f, // Channels
                    g = Color.green(pixels[i]) / 255.0f,
                    b = Color.blue(pixels[i]) / 255.0f;

            /*
             * c = a * f + (1 - a) * b => a = (c - 1 * b) / (f - b)
             * Where c - Output RGB channel
             *       a - Foreground alpha channel
             *       f - Foreground RGB channel
             *       b - Background RGB channel
             */
            final float a_ = (dr == 0.0f ? 0.0f : (r - fa * br) / dr * rr)
                    + (dg == 0.0f ? 0.0f : (g - fa * bg) / dg * rg)
                    + (db == 0.0f ? 0.0f : (b - fa * bb) / db * rb);

            pixels[i] = Color.argb(saturate(a_), fr, fg, fb);
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private static float saturate(float v) {
        return v <= 0.0f ? 0.0f : v >= 1.0f ? 1.0f : v;
    }
}
