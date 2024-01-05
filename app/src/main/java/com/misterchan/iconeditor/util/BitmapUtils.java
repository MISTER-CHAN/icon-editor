package com.misterchan.iconeditor.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;

import com.misterchan.iconeditor.ColorRange;
import com.misterchan.iconeditor.Settings;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.function.Function;

public class BitmapUtils {
    private static final Function<Integer, Integer>[] FUNC_RGBA = new Function[]{
            (Function<Integer, Integer>) Color::red,
            (Function<Integer, Integer>) Color::green,
            (Function<Integer, Integer>) Color::blue,
            (Function<Integer, Integer>) Color::alpha,
    };

    /**
     * Displaying colors
     */
    @ColorInt
    private static final int[] DC_RGBA = {Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};

    public static final Paint PAINT_CLEAR = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.CLEAR);
            setFilterBitmap(false);
        }
    };

    public static final Paint PAINT_SRC = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    public static final Paint PAINT_SRC_OVER = new Paint() {
        {
            setAntiAlias(false);
            setFilterBitmap(false);
        }
    };

    private BitmapUtils() {
    }

    public static void addLightingColorFilter(@ColorInt final int[] src, @ColorInt final int[] dst,
                                              final float mul, final float add) {
        for (int i = 0; i < src.length; ++i) {
            final int r = Color.red(src[i]), g = Color.green(src[i]), b = Color.blue(src[i]);
            final int r_ = ColorUtils.sat((int) (r * mul + add));
            final int g_ = ColorUtils.sat((int) (g * mul + add));
            final int b_ = ColorUtils.sat((int) (b * mul + add));
            dst[i] = ColorUtils.clipped(src[i], r_, g_, b_);
        }
    }

    public static void addLightingColorFilter(final Bitmap bitmap, final Rect rect,
                                              @Size(8) final float[] lighting) {
        final int w = rect.width(), h = rect.height();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
        addLightingColorFilter(pixels, pixels, lighting);
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    public static void addLightingColorFilter(@ColorInt final int[] src, @ColorInt final int[] dst,
                                              @Size(8) final float[] lighting) {
        for (int i = 0; i < src.length; ++i) {
            final int r = Color.red(src[i]), g = Color.green(src[i]), b = Color.blue(src[i]), a = Color.alpha(src[i]);
            final int r_ = ColorUtils.sat((int) (r * lighting[0] + lighting[1]));
            final int g_ = ColorUtils.sat((int) (g * lighting[2] + lighting[3]));
            final int b_ = ColorUtils.sat((int) (b * lighting[4] + lighting[5]));
            final int a_ = ColorUtils.sat((int) (a * lighting[6] + lighting[7]));
            dst[i] = Color.argb(a_, r_, g_, b_);
        }
    }

    public static void addColorMatrixColorFilter(final Bitmap bitmap, final Rect rect,
                                                 @Size(20) final float[] colorMatrix) {
        final int w = rect.width(), h = rect.height();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
        addColorMatrixColorFilter(pixels, pixels, colorMatrix);
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    /**
     * A faster way to add a color matrix color filter.
     */
    public static void addColorMatrixColorFilter(@ColorInt final int[] src, @ColorInt final int[] dst,
                                                 @Size(20) final float[] colorMatrix) {
        for (int i = 0; i < src.length; ++i) {
            final int r = Color.red(src[i]), g = Color.green(src[i]), b = Color.blue(src[i]), a = Color.alpha(src[i]);
            final int r_ = ColorUtils.sat((int) (r * colorMatrix[0] + g * colorMatrix[1] + b * colorMatrix[2] + a * colorMatrix[3] + colorMatrix[4]));
            final int g_ = ColorUtils.sat((int) (r * colorMatrix[5] + g * colorMatrix[6] + b * colorMatrix[7] + a * colorMatrix[8] + colorMatrix[9]));
            final int b_ = ColorUtils.sat((int) (r * colorMatrix[10] + g * colorMatrix[11] + b * colorMatrix[12] + a * colorMatrix[13] + colorMatrix[14]));
            final int a_ = ColorUtils.sat((int) (r * colorMatrix[15] + g * colorMatrix[16] + b * colorMatrix[17] + a * colorMatrix[18] + colorMatrix[19]));
            dst[i] = Color.argb(a_, r_, g_, b_);
        }
    }

    public static void applyCurves(final Bitmap bitmap, final Rect rect, @Size(5) final int[][] curves) {
        final int w = rect.width(), h = rect.height();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
        applyCurves(pixels, pixels, curves);
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    public static void applyCurves(@ColorInt final int[] src, @ColorInt final int[] dst, @Size(5) final int[][] curves) {
        for (int i = 0; i < src.length; ++i) {
            final int pixel = src[i];
            final int a = Color.alpha(pixel),
                    r = Color.red(pixel), g = Color.green(pixel), b = Color.blue(pixel);
            dst[i] = Color.argb(curves[3][a],
                    curves[4][curves[0][r]], curves[4][curves[1][g]], curves[4][curves[2][b]]);
        }
    }

    public static void bucketFill(final Bitmap src, Rect srcRect, final Bitmap dst, Rect dstRect,
                                  final int x, final int y, @ColorInt final int color,
                                  final boolean ignoreAlpha, final int tolerance,
                                  @Nullable final Rect bounds) {
        if (dstRect == null) {
            dstRect = new Rect(0, 0, dst.getWidth(), dst.getHeight());
        }
        final int w = dstRect.width(), h = dstRect.height();
        if (srcRect == null) {
            srcRect = src == dst ? dstRect : new Rect(0, 0, src.getWidth(), src.getHeight());
        }
        if (src != dst && (srcRect.width() != w || srcRect.height() != h)) {
            return;
        }
        if (!(dstRect.left <= x && x < dstRect.right && dstRect.top <= y && y < dstRect.bottom)) {
            return;
        }
        if (src != dst) {
            final int srcX = x + srcRect.left - dstRect.left, srcY = y + srcRect.top - dstRect.top;
            if (!(dstRect.left <= srcX && srcX < dstRect.right && dstRect.top <= srcY && srcY < dstRect.bottom)) {
                return;
            }
        }
        @ColorInt final int pixel = src.getPixel(x, y);
        if (pixel == color && tolerance == 0) {
            return;
        }
        @ColorInt final int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, srcRect.left, srcRect.top, w, h);
        for (int i = 0; i < pixels.length; ++i) {
            final int px = pixels[i];
            if (ignoreAlpha) {
                if (tolerance == 0 ?
                        ColorUtils.rgb(px) == ColorUtils.rgb(pixel) :
                        ColorUtils.matches(pixel, px, tolerance)) {
                    pixels[i] = ColorUtils.clipped(px, color);
                }
            } else {
                if (tolerance == 0 ?
                        px == pixel :
                        Color.alpha(px) == Color.alpha(pixel)
                                && ColorUtils.matches(pixel, px, tolerance)) {
                    pixels[i] = color;
                }
            }
            if (pixels[i] != px && bounds != null) {
                bounds.union(i % w, i / w, i % w + 1, i / w + 1);
            }
        }
        dst.setPixels(pixels, 0, w, dstRect.left, dstRect.top, w, h);
    }

    public static void clip(final Bitmap bitmap, final Rect rect, @ColorInt final int[] base) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
        for (int i = 0; i < pixels.length; ++i) {
            pixels[i] = ColorUtils.clipped(base[i], pixels[i]);
        }
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    public static Bitmap createBitmap(Bitmap src) {
        return createBitmap(src, null);
    }

    public static Bitmap createBitmap(Bitmap src, int x, int y, int width, int height) {
        return createBitmap(src, new Rect(x, y, x + width, y + height));
    }

    public static Bitmap createBitmap(Bitmap src, @Nullable Rect rect) {
        return createBitmap(src, rect, Bitmap.Config.ARGB_8888, true, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    /**
     * A more simple way to create a bitmap.
     */
    public static Bitmap createBitmap(Bitmap src, @Nullable Rect rect, Bitmap.Config config, boolean hasAlpha, ColorSpace colorSpace) {
        final int w = rect != null ? rect.width() : src.getWidth(), h = rect != null ? rect.height() : src.getHeight();
        final Bitmap dst = Bitmap.createBitmap(w, h, config, hasAlpha, colorSpace);
        final Canvas dstCv = new Canvas(dst);
        if (rect != null) {
            dstCv.drawBitmap(src, rect, new Rect(0, 0, w, h), PAINT_SRC);
        } else {
            dstCv.drawBitmap(src, 0.0f, 0.0f, PAINT_SRC);
        }
        return dst;
    }

    public static Bitmap drawableToBitmap(@NonNull Context context, @DrawableRes int id) {
        final Drawable drawable = ContextCompat.getDrawable(context, id);
        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void drawHistogram(@ColorInt int[] src, Bitmap dst,
                                     @IntRange(from = 0, to = 4) int comp, ImageView iv) {
        new Thread(() -> {
            final int compMin = comp == 4 ? 0 : comp, compMax = comp == 4 ? 2 : comp;
            final int[][] numValue = new int[4][];
            int max = 1;
            for (int c = compMin; c <= compMax; ++c) {
                numValue[c] = new int[0x100];
                for (final int pixel : src) {
                    final int n = ++numValue[c][FUNC_RGBA[c].apply(pixel)];
                    if (n > max) max = n;
                }
            }
            final Canvas cv = new Canvas(dst);
            final float maxHeight = dst.getHeight();
            final Paint paint = new Paint(PAINT_SRC_OVER);
            paint.setBlendMode(comp == 4 ? BlendMode.PLUS : BlendMode.SRC);
            for (int c = compMin; c <= compMax; ++c) {
                paint.setColor(DC_RGBA[c]);
                for (int i = 0x0; i < 0x100; ) {
                    cv.drawRect(i, maxHeight - (float) numValue[c][i] / (float) max * maxHeight,
                            ++i, maxHeight,
                            paint);
                }
            }
            iv.invalidate();
        }).start();
    }

    private static Bitmap edgeDetection(final Bitmap bitmap) {
        return null;
    }

    public static void fillInBlank(final Bitmap src, final Bitmap dst) {
        final int w = dst.getWidth(), h = dst.getHeight(), area = w * h;
        final int[] dstPixels = new int[area], srcPixels = new int[area];
        src.getPixels(srcPixels, 0, w, 0, 0, w, h);
        dst.getPixels(dstPixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            final int sa = Color.alpha(srcPixels[i]), sr = Color.red(srcPixels[i]), sg = Color.green(srcPixels[i]), sb = Color.blue(srcPixels[i]);
            final int da = Color.alpha(dstPixels[i]), dr = Color.red(dstPixels[i]), dg = Color.green(dstPixels[i]), db = Color.blue(dstPixels[i]);
            final int da_ = Math.max(0x00, da - sa);
            final int dr_ = (da_ * dr + sa * sr) / 0xFF, dg_ = (da_ * dg + sa * sg) / 0xFF, db_ = (da_ * db + sa * sb) / 0xFF;
            dstPixels[i] = Color.argb(da, dr_, dg_, db_);
        }
        dst.setPixels(dstPixels, 0, w, 0, 0, w, h);
    }

    public static void floodFill(final Bitmap src, Rect srcRect, final Bitmap dst, Rect dstRect,
                                 final int x, final int y, @ColorInt final int color,
                                 final boolean ignoreAlpha, final int tolerance) {
        floodFill(src, srcRect, dst, dstRect, x, y, color, ignoreAlpha, tolerance, null);
    }

    public static void floodFill(final Bitmap src, Rect srcRect, final Bitmap dst, Rect dstRect,
                                 final int x, final int y, @ColorInt final int color,
                                 final boolean ignoreAlpha, final int tolerance,
                                 final Rect bounds) {
        if (dstRect == null) {
            dstRect = new Rect(0, 0, dst.getWidth(), dst.getHeight());
        }
        final int w = dstRect.width(), h = dstRect.height(), area = w * h;
        if (srcRect == null) {
            srcRect = src == dst ? dstRect : new Rect(0, 0, src.getWidth(), src.getHeight());
        }
        if (src != dst && (srcRect.width() != w || srcRect.height() != h)) {
            return;
        }
        if (!(dstRect.left <= x && x < dstRect.right && dstRect.top <= y && y < dstRect.bottom)) {
            return;
        }
        if (src != dst) {
            final int srcX = x + srcRect.left - dstRect.left, srcY = y + srcRect.top - dstRect.top;
            if (!(srcRect.left <= srcX && srcX < srcRect.right && srcRect.top <= srcY && srcY < srcRect.bottom)) {
                return;
            }
        }
        @ColorInt final int pixel = src.getPixel(x, y);
        if (pixel == color && tolerance == 0) {
            return;
        }
        @ColorInt final int[] srcPixels = new int[area], dstPixels = src == dst ? srcPixels : new int[area];
        src.getPixels(srcPixels, 0, w, srcRect.left, srcRect.top, w, h);
        if (src != dst) {
            dst.getPixels(dstPixels, 0, w, dstRect.left, dstRect.top, w, h);
        }
//      final long a = System.currentTimeMillis();
        final Queue<Point> queue = new LinkedList<>();
        final boolean[] visited = new boolean[area];
        queue.offer(new Point(x, y));
        Point point;
        while ((point = queue.poll()) != null) {
            final int i = (point.y - dstRect.top) * w + (point.x - dstRect.left);
            if (visited[i]) {
                continue;
            }
            visited[i] = true;
            final int px = srcPixels[i];
            final boolean match;
            final int newColor;
            if (ignoreAlpha) {
                match = tolerance == 0
                        ? ColorUtils.rgb(px) == ColorUtils.rgb(pixel)
                        : ColorUtils.matches(pixel, px, tolerance);
                newColor = ColorUtils.clipped(px, color);
            } else {
                match = tolerance == 0 ?
                        px == pixel :
                        Color.alpha(px) == Color.alpha(pixel)
                                && ColorUtils.matches(pixel, px, tolerance);
                newColor = color;
            }
            if (match) {
                srcPixels[i] = newColor;
                if (src != dst) {
                    dstPixels[i] = newColor;
                }
                if (bounds != null) {
                    bounds.union(point.x, point.y, point.x + 1, point.y + 1);
                }
                if (dstRect.left <= point.x - 1 && !visited[i - 1])
                    queue.offer(new Point(point.x - 1, point.y));
                if (dstRect.top <= point.y - 1 && !visited[i - w])
                    queue.offer(new Point(point.x, point.y - 1));
                if (point.x + 1 < dstRect.right && !visited[i + 1])
                    queue.offer(new Point(point.x + 1, point.y));
                if (point.y + 1 < dstRect.bottom && !visited[i + w])
                    queue.offer(new Point(point.x, point.y + 1));
            }
        }
//      final long b = System.currentTimeMillis();
        dst.setPixels(dstPixels, 0, w, dstRect.left, dstRect.top, w, h);
    }

    public static void generateNoise(@ColorInt final int[] pixels, @ColorInt final int color,
                                     @FloatRange(from = 0.0f, to = 1.0f) final float noisiness, final Long seed,
                                     final boolean noRepeats) {
        final Random random = seed == null ? new Random() : new Random(seed);
        if (noRepeats) {
            for (int i = 0; i < pixels.length; ++i) {
                if (random.nextFloat() < noisiness) {
                    pixels[i] = color;
                }
            }
        } else {
            final int amount = (int) (pixels.length * noisiness);
            for (int i = 0; i < amount; ++i) {
                pixels[(int) (pixels.length * random.nextFloat())] = color;
            }
        }
    }

    public static void generateNoise(final Canvas canvas, final Rect rect, final Paint paint,
                                     @FloatRange(from = 0.0f, to = 1.0f) final float noisiness, final Long seed,
                                     final boolean noRepeats) {
        final Random random = seed == null ? new Random() : new Random(seed);
        if (noRepeats) {
            for (float y = rect.top + 0.5f; y < rect.bottom; ++y) {
                for (float x = rect.left + 0.5f; x < rect.right; ++x) {
                    if (random.nextFloat() < noisiness) {
                        canvas.drawPoint(x, y, paint);
                    }
                }
            }
        } else {
            final int amount = (int) (rect.width() * rect.height() * noisiness);
            for (int i = 0; i < amount; ++i) {
                canvas.drawPoint(rect.left + rect.width() * random.nextFloat(),
                        rect.top + rect.height() * random.nextFloat(), paint);
            }
        }
    }

    public static void generateNoise(final Canvas canvas, final Rect rect, final Bitmap bitmap, final Paint paint,
                                     @FloatRange(from = 0.0f, to = 1.0f) final float noisiness, final Long seed,
                                     final boolean noRepeats) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final Random random = seed == null ? new Random() : new Random(seed);
        if (noRepeats) {
            for (float y = rect.top - w; y < rect.bottom; ++y) {
                for (float x = rect.left - h; x < rect.right; ++x) {
                    if (random.nextFloat() < noisiness) {
                        canvas.drawBitmap(bitmap, x, y, paint);
                    }
                }
            }
        } else {
            final int amount = (int) (rect.width() * rect.height() * noisiness);
            for (int i = 0; i < amount; ++i) {
                canvas.drawBitmap(bitmap,
                        (rect.width() + w) * random.nextFloat() - w + rect.left,
                        (rect.height() + h) * random.nextFloat() - h + rect.top,
                        paint);
            }
        }
    }

    @ColorInt
    public static int[] getPixels(final Bitmap bitmap) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        return pixels;
    }

    @ColorInt
    public static int[] getPixels(final Bitmap bitmap, Rect rect) {
        final int w = rect.width(), h = rect.height();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
        return pixels;
    }

    public static void mergeAlpha(final Bitmap src, final Bitmap dst) {
        final int w = Math.min(src.getWidth(), dst.getWidth()), h = Math.min(src.getHeight(), dst.getHeight()), area = w * h;
        final int[] srcPixels = new int[area], dstPixels = new int[area];
        src.getPixels(srcPixels, 0, w, 0, 0, w, h);
        dst.getPixels(dstPixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            dstPixels[i] = ColorUtils.clipped(srcPixels[i], dstPixels[i]);
        }
        dst.setPixels(dstPixels, 0, w, 0, 0, w, h);
    }

    /**
     * Makes a mutable copy then recycle the source bitmap.
     */
    public static Bitmap mutable(Bitmap bitmap) {
        final Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), true);
        bitmap.recycle();
        if (Settings.INST.autoSetHasAlpha()) {
            newBitmap.setHasAlpha(true);
        }
        return newBitmap;
    }

    public static void posterize(Bitmap bitmap, Rect rect, @IntRange(from = 0x01, to = 0xFF) int level) {
        final int w = rect.width(), h = rect.height();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
        posterize(pixels, pixels, level);
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    public static void posterize(@ColorInt int[] src, @ColorInt int[] dst, @IntRange(from = 0x01, to = 0xFF) int level) {
        for (int i = 0; i < src.length; ++i) {
            // Inputs
            final int ir = Color.red(src[i]), ig = Color.green(src[i]), ib = Color.blue(src[i]);

            // Outputs
            final int or = Math.round(ir / 255.0f * (level - 1.0f)) * 0xFF / (level - 1),
                    og = Math.round(ig / 255.0f * (level - 1.0f)) * 0xFF / (level - 1),
                    ob = Math.round(ib / 255.0f * (level - 1.0f)) * 0xFF / (level - 1);

            dst[i] = ColorUtils.clipped(src[i], or, og, ob);
        }
    }

    public static void recycle(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
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

            pixels[i] = Color.argb(ColorUtils.sat(a_), fr, fg, fb);
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private static void scaleAlpha(Bitmap bitmap) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        @ColorInt final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < pixels.length; ++i) {
            if (pixels[i] > 0x00FFFFFF) {
                pixels[i] |= Color.BLACK;
            }
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    public static void selectByColorRange(final Bitmap bitmap, @Nullable final Rect rect, final ColorRange cr) {
        if (!cr.enabled) {
            return;
        }
        final int w = rect != null ? rect.width() : bitmap.getWidth(), h = rect != null ? rect.height() : bitmap.getHeight();
        final int[] pixels = new int[w * h];
        final int x = rect != null ? rect.left : 0, y = rect != null ? rect.top : 0;
        bitmap.getPixels(pixels, 0, w, x, y, w, h);
        selectByColorRange(pixels, pixels, cr);
        bitmap.setPixels(pixels, 0, w, x, y, w, h);
    }

    public static void selectByColorRange(@ColorInt final int[] src, @ColorInt final int[] dst,
                                          final ColorRange cr) {
        final float[] hsv = new float[3];
        for (int i = 0; i < src.length; ++i) {
            ColorUtils.colorToHSV(src[i], hsv);
            float a_ = 0.0f;
            final float ao3 = Color.alpha(src[i]) / 255.0f / 3.0f; // Alpha over 3
            float hi = 0.0f, ha = 0.0f; // Hue min and max
            if (cr.transition > 0.0f) {
                hi = cr.cuboid[0] - cr.transition * 360.0f;
                ha = cr.cuboid[3] + cr.transition * 360.0f;
                if (hi > ha) {
                    if (hsv[0] < ha) hi -= 360.0f;
                    if (hsv[0] > hi) ha += 360.0f;
                }
            }
            a_ += cr.transition > 0.0f
                    ? Math.min(Math.min(hsv[0] - hi, ha - hsv[0]) / (cr.transition * 360.0f), 1.0f) * ao3
                    : (cr.cuboid[0] <= cr.cuboid[3] ? cr.cuboid[0] <= hsv[0] && hsv[0] <= cr.cuboid[3] : cr.cuboid[0] <= hsv[0] || hsv[0] <= cr.cuboid[3]) ? ao3 : ao3 * -2;
            a_ += cr.transition > 0.0f
                    ? Math.min(Math.min(hsv[1] - (cr.cuboid[1] - cr.transition), (cr.cuboid[4] + cr.transition) - hsv[1]) / cr.transition, 1.0f) * ao3
                    : cr.cuboid[1] <= hsv[1] && hsv[1] <= cr.cuboid[4] ? ao3 : ao3 * -2;
            a_ += cr.transition > 0.0f
                    ? Math.min(Math.min(hsv[2] - (cr.cuboid[2] - cr.transition), (cr.cuboid[5] + cr.transition) - hsv[2]) / cr.transition, 1.0f) * ao3
                    : cr.cuboid[2] <= hsv[2] && hsv[2] <= cr.cuboid[5] ? ao3 : ao3 * -2;
            dst[i] = ColorUtils.argb((int) (Math.max(a_, 0.0f) * 255.0f), ColorUtils.rgb(src[i]));
        }
    }

    public static void setAlphaByHue(@ColorInt final int[] src, @ColorInt final int[] dst,
                                     final float opaquePoint) {
        final float op = opaquePoint % 360.0f;
        for (int i = 0; i < src.length; ++i) {
            final int pixel = src[i];
            final float hue = ColorUtils.hue(pixel);
            final float smaller = Math.min(op, hue), greater = Math.max(op, hue);
            final float majorArc = Math.max(greater - smaller, 360.0f + smaller - greater);
            dst[i] = ColorUtils.argb((int) ((majorArc - 180.0f) / 180.0f * 0xFF), ColorUtils.rgb(pixel));
        }
    }

    public static void shiftHs(@ColorInt final int[] src, @ColorInt final int[] dst,
                               @Size(4) final float[] deltaHs) {
//      long a = System.currentTimeMillis();
        if (deltaHs[3] == 1) shiftHsl(src, dst, deltaHs);
        else shiftHsv(src, dst, deltaHs);
//      long b = System.currentTimeMillis();
    }

    public static void shiftHs(final Bitmap bitmap, final Rect rect, @Size(4) final float[] deltaHs) {
        final int w = rect.width(), h = rect.height();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
        shiftHs(pixels, pixels, deltaHs);
        bitmap.setPixels(pixels, 0, w, rect.left, rect.top, w, h);
    }

    public static void shiftHsl(@ColorInt final int[] src, @ColorInt final int[] dst,
                                @Size(4) final float[] deltaHsl) {
        final float[] hsl = new float[3];
        for (int i = 0; i < src.length; ++i) {
            final int pixel = src[i];
            ColorUtils.colorToHSL(pixel, hsl);
            hsl[0] = (hsl[0] + deltaHsl[0] + 360.0f) % 360.0f;
            hsl[1] = ColorUtils.sat(hsl[1] + deltaHsl[1]);
            hsl[2] = ColorUtils.sat(hsl[2] + deltaHsl[2]);
            dst[i] = ColorUtils.clipped(pixel, ColorUtils.HSLToColor(hsl));
        }
    }

    public static void shiftHsv(@ColorInt final int[] src, @ColorInt final int[] dst,
                                @Size(4) final float[] deltaHsv) {
        final float[] hsv = new float[3];
        for (int i = 0; i < src.length; ++i) {
            final int pixel = src[i];
            ColorUtils.colorToHSV(pixel, hsv);
            hsv[0] = (hsv[0] + deltaHsv[0] + 360.0f) % 360.0f;
            hsv[1] = ColorUtils.sat(hsv[1] + deltaHsv[1]);
            hsv[2] = ColorUtils.sat(hsv[2] + deltaHsv[2]);
            dst[i] = ColorUtils.clipped(pixel, ColorUtils.HSVToColor(hsv));
        }
    }

    public static void whiteBalance(@ColorInt final int[] src, @ColorInt int[] dst, @ColorInt int white) {
    }
}
