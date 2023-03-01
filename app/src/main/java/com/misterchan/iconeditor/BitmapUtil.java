package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

class BitmapUtil {

    public static void addLightingColorFilter(final Bitmap src, final int srcX, final int srcY,
                                              final Bitmap dst, final int dstX, final int dstY,
                                              final float scale, final float shift) {
        final int w = src.getWidth(), h = src.getHeight();
        final int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, srcX, srcY, w, h);
        addLightingColorFilter(pixels, pixels, scale, shift);
        dst.setPixels(pixels, 0, w, dstX, dstY, w, h);
    }

    public static void addLightingColorFilter(@ColorInt final int[] src, @ColorInt final int[] dst,
                                              final float scale, final float shift) {
        for (int i = 0; i < src.length; ++i) {
            final int r = Color.red(src[i]), g = Color.green(src[i]), b = Color.blue(src[i]),
                    a = src[i] & Color.BLACK;
            final int r_ = Color.sat((int) (r * scale + shift));
            final int g_ = Color.sat((int) (g * scale + shift));
            final int b_ = Color.sat((int) (b * scale + shift));
            dst[i] = a | Color.rgb(r_, g_, b_);
        }
    }

    public static void addLightingColorFilter(@ColorInt final int[] src, @ColorInt final int[] dst,
                                              @Size(8) float[] lighting) {
        for (int i = 0; i < src.length; ++i) {
            final int r = Color.red(src[i]), g = Color.green(src[i]), b = Color.blue(src[i]),
                    a = Color.alpha(src[i]);
            final int r_ = Color.sat((int) (r * lighting[0] + lighting[1]));
            final int g_ = Color.sat((int) (g * lighting[2] + lighting[3]));
            final int b_ = Color.sat((int) (b * lighting[4] + lighting[5]));
            final int a_ = Color.sat((int) (a * lighting[6] + lighting[7]));
            dst[i] = Color.argb(a_, r_, g_, b_);
        }
    }

    public static void addColorMatrixColorFilter(final Bitmap src, final int srcX, final int srcY,
                                                 final Bitmap dst, final int dstX, final int dstY,
                                                 @Size(20) final float[] colorMatrix) {
        final int w = src.getWidth(), h = src.getHeight();
        final int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, srcX, srcY, w, h);
        addColorMatrixColorFilter(pixels, pixels, colorMatrix);
        dst.setPixels(pixels, 0, w, dstX, dstY, w, h);
    }

    public static void addColorMatrixColorFilter(@ColorInt final int[] src, @ColorInt final int[] dst,
                                                 @Size(20) final float[] colorMatrix) {
        for (int i = 0; i < src.length; ++i) {
            final int r = Color.red(src[i]), g = Color.green(src[i]), b = Color.blue(src[i]),
                    a = Color.alpha(src[i]);
            final int r_ = Color.sat((int) (r * colorMatrix[0] + g * colorMatrix[1] + b * colorMatrix[2] + a * colorMatrix[3] + colorMatrix[4]));
            final int g_ = Color.sat((int) (r * colorMatrix[5] + g * colorMatrix[6] + b * colorMatrix[7] + a * colorMatrix[8] + colorMatrix[9]));
            final int b_ = Color.sat((int) (r * colorMatrix[10] + g * colorMatrix[11] + b * colorMatrix[12] + a * colorMatrix[13] + colorMatrix[14]));
            final int a_ = Color.sat((int) (r * colorMatrix[15] + g * colorMatrix[16] + b * colorMatrix[17] + a * colorMatrix[18] + colorMatrix[19]));
            dst[i] = Color.argb(a_, r_, g_, b_);
        }
    }

    public static void applyCurves(final Bitmap bitmap, @Size(5) int[][] curves) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        applyCurves(pixels, pixels, curves);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    public static void applyCurves(@ColorInt final int[] src, @ColorInt final int[] dst, @Size(5) int[][] curves) {
        for (int i = 0; i < src.length; ++i) {
            final int pixel = src[i];
            final int a = Color.alpha(pixel),
                    r = Color.red(pixel), g = Color.green(pixel), b = Color.blue(pixel);
            dst[i] = Color.argb(curves[3][a],
                    curves[4][curves[0][r]], curves[4][curves[1][g]], curves[4][curves[2][b]]);
        }
    }

    public static void bucketFill(final Bitmap bitmap, Rect rect, int x, int y, @ColorInt final int color) {
        bucketFill(bitmap, rect, x, y, color, false, 0);
    }

    public static void bucketFill(final Bitmap bitmap, Rect rect, int x, int y, @ColorInt final int color,
                                  final boolean ignoreAlpha, final int tolerance) {
        if (rect == null) {
            rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        } else if (!(rect.left <= x && x < rect.right && rect.top <= y && y < rect.bottom)) {
            return;
        }
        final int pixel = bitmap.getPixel(x, y);
        if (pixel == color && tolerance == 0) {
            return;
        }
        final int w = rect.right - rect.left, h = rect.bottom - rect.top;
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
        for (int i = 0; i < pixels.length; ++i) {
            final int px = pixels[i];
            if (ignoreAlpha) {
                if (tolerance == 0 ?
                        Color.rgb(px) == Color.rgb(pixel) :
                        Color.isPermissible(pixel, tolerance, px)) {
                    pixels[i] = px & Color.BLACK | Color.rgb(color);
                }
            } else {
                if (tolerance == 0 ?
                        px == pixel :
                        Color.alpha(px) == Color.alpha(pixel)
                                && Color.isPermissible(pixel, px, tolerance)) {
                    pixels[i] = color;
                }
            }
        }
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    private static Bitmap edgeDetection(final Bitmap bitmap) {
        return null;
    }

    public static void floodFill(final Bitmap bitmap, Rect rect, int x, int y, @ColorInt final int color) {
        floodFill(bitmap, bitmap, rect, x, y, color, false, 0);
    }

    public static void floodFill(final Bitmap bitmap, Rect rect, int x, int y, @ColorInt final int color,
                                 final boolean ignoreAlpha, final int tolerance) {
        floodFill(bitmap, bitmap, rect, x, y, color, ignoreAlpha, tolerance);
    }

    public static void floodFill(final Bitmap src, final Bitmap dst, Rect rect,
                                 int x, int y, @ColorInt final int color,
                                 final boolean ignoreAlpha, final int tolerance) {
        if (rect == null) {
            rect = new Rect(0, 0, src.getWidth(), src.getHeight());
        } else if (!(rect.left <= x && x < rect.right && rect.top <= y && y < rect.bottom)) {
            return;
        }
        final int pixel = src.getPixel(x, y);
        if (pixel == color && tolerance == 0) {
            return;
        }
        final int w = rect.right - rect.left, h = rect.bottom - rect.top, area = w * h;
        final int[] srcPixels = new int[area], dstPixels = src == dst ? srcPixels : new int[area];
        src.getPixels(srcPixels, 0, w, rect.left, rect.top, w, h);
//      final long a = System.currentTimeMillis();
        final Queue<Point> pointsToSet = new LinkedList<>();
        final boolean[] havePointsBeenSet = new boolean[area];
        pointsToSet.offer(new Point(x, y));
        Point point;
        while ((point = pointsToSet.poll()) != null) {
            final int i = (point.y - rect.top) * w + (point.x - rect.left);
            if (havePointsBeenSet[i]) {
                continue;
            }
            havePointsBeenSet[i] = true;
            final int px = srcPixels[i];
            boolean match;
            int newColor;
            if (ignoreAlpha) {
                match = tolerance == 0
                        ? Color.rgb(px) == Color.rgb(pixel)
                        : Color.isPermissible(pixel, px, tolerance);
                newColor = px & Color.BLACK | Color.rgb(color);
            } else {
                match = tolerance == 0 ?
                        px == pixel :
                        Color.alpha(px) == Color.alpha(pixel)
                                && Color.isPermissible(pixel, px, tolerance);
                newColor = color;
            }
            if (match) {
                srcPixels[i] = newColor;
                if (src != dst) {
                    dstPixels[i] = newColor;
                }
                final int xn = point.x - 1, xp = point.x + 1, yn = point.y - 1, yp = point.y + 1; // n - negative, p - positive
                if (rect.left <= xn && !havePointsBeenSet[i - 1])
                    pointsToSet.offer(new Point(xn, point.y));
                if (xp < rect.right && !havePointsBeenSet[i + 1])
                    pointsToSet.offer(new Point(xp, point.y));
                if (rect.top <= yn && !havePointsBeenSet[i - w])
                    pointsToSet.offer(new Point(point.x, yn));
                if (yp < rect.bottom && !havePointsBeenSet[i + w])
                    pointsToSet.offer(new Point(point.x, yp));
            }
        }
//      final long b = System.currentTimeMillis();
//      Toast.makeText(this, String.valueOf(b - a), Toast.LENGTH_SHORT).show();
        dst.setPixels(dstPixels, 0, w, rect.left, rect.top, w, h);
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

    public static void mergeAlpha(final Bitmap src, final Bitmap dst) {
        final int w = Math.min(src.getWidth(), dst.getWidth()), h = Math.min(src.getHeight(), dst.getHeight()), area = w * h;
        final int[] srcPixels = new int[area], dstPixels = new int[area];
        src.getPixels(srcPixels, 0, w, 0, 0, w, h);
        dst.getPixels(dstPixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            dstPixels[i] = srcPixels[i] & Color.BLACK | Color.rgb(dstPixels[i]);
        }
        dst.setPixels(dstPixels, 0, w, 0, 0, w, h);
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

            pixels[i] = Color.argb(Color.sat(a_), fr, fg, fb);
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private static void scaleAlpha(Bitmap bitmap) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < pixels.length; ++i) {
            if (Color.alpha(pixels[i]) > 0x00) {
                pixels[i] |= Color.BLACK;
            }
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    public static void setAlphaByHue(@ColorInt final int[] src, @ColorInt final int[] dst,
                                     final float opaquePoint) {
        final float op = opaquePoint % 360.0f;
        for (int i = 0; i < src.length; ++i) {
            final int pixel = src[i];
            final float hue = Color.hue(pixel);
            final float smaller = Math.min(op, hue), greater = Math.max(op, hue);
            final float majorArc = Math.max(greater - smaller, 360.0f + smaller - greater);
            dst[i] = (int) ((majorArc - 180.0f) / 180.0f * 0xFF) << 24 | Color.rgb(pixel);
        }
    }

    public static void shiftHsv(@ColorInt final int[] src, @ColorInt final int[] dst,
                                @Size(3) final float[] deltaHSV) {
        final float[] hsv = new float[3];
        for (int i = 0; i < src.length; ++i) {
            final int pixel = src[i];
            Color.colorToHSV(pixel, hsv);
            hsv[0] = (hsv[0] + deltaHSV[0] + 360.0f) % 360.0f;
            hsv[1] = Color.sat(hsv[1] + deltaHSV[1]);
            hsv[2] = Color.sat(hsv[2] + deltaHSV[2]);
            dst[i] = pixel & Color.BLACK | Color.HSVToColor(hsv);
        }
    }

    public static void shiftHsv(final Bitmap bitmap, @Size(3) final float[] deltaHSV) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        shiftHsv(pixels, pixels, deltaHSV);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    public static void whiteBalance(@ColorInt final int[] src, @ColorInt int[] dst, @ColorInt int white) {
    }
}
