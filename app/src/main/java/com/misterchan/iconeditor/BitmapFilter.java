package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.Size;

public class BitmapFilter {

    public static void addColorFilter(final Bitmap src, final int srcX, final int srcY,
                                      final Bitmap dst, final int dstX, final int dstY,
                                      final float scale, final float shift) {
        final int w = src.getWidth(), h = src.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        src.getPixels(pixels, 0, w, srcX, srcY, w, h);
        for (int i = 0; i < area; ++i) {
            final int r = android.graphics.Color.red(pixels[i]), g = android.graphics.Color.green(pixels[i]), b = android.graphics.Color.blue(pixels[i]),
                    a = android.graphics.Color.alpha(pixels[i]);
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
            final int r = android.graphics.Color.red(pixels[i]), g = android.graphics.Color.green(pixels[i]), b = android.graphics.Color.blue(pixels[i]),
                    a = android.graphics.Color.alpha(pixels[i]);
            final int r_ = inRangeFrom0To255((int) (r * colorMatrix[0] + g * colorMatrix[1] + b * colorMatrix[2] + a * colorMatrix[3] + colorMatrix[4]));
            final int g_ = inRangeFrom0To255((int) (r * colorMatrix[5] + g * colorMatrix[6] + b * colorMatrix[7] + a * colorMatrix[8] + colorMatrix[9]));
            final int b_ = inRangeFrom0To255((int) (r * colorMatrix[10] + g * colorMatrix[11] + b * colorMatrix[12] + a * colorMatrix[13] + colorMatrix[14]));
            final int a_ = inRangeFrom0To255((int) (r * colorMatrix[15] + g * colorMatrix[16] + b * colorMatrix[17] + a * colorMatrix[18] + colorMatrix[19]));
            pixels[i] = android.graphics.Color.argb(a_, r_, g_, b_);
        }
        dst.setPixels(pixels, 0, w, dstX, dstY, w, h);
    }

    private static int inRangeFrom0To255(int a) {
        return Math.max(Math.min(a, 255), 0);
    }
}
