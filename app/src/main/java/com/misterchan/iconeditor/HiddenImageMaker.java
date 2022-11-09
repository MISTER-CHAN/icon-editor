package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.widget.SeekBar;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

public class HiddenImageMaker {

    public interface OnFinishSettingListener {
        void onFinish(Bitmap bitmap);
    }

    private AlertDialog dialog;
    private SeekBar sbScaleToBlack, sbScaleToWhite;

    public static void merge(Context context, @Size(2) final Bitmap[] bitmaps,
                             final OnFinishSettingListener listener) {

        final HiddenImageMaker merger = new HiddenImageMaker();

        merger.dialog = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.merge_as_a_hidden_image)
                .setView(R.layout.merge_as_hidden)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    float[] scale = new float[]{
                            merger.sbScaleToWhite.getProgress() / 8.0f,
                            merger.sbScaleToBlack.getProgress() / 8.0f
                    };
                    listener.onFinish(mergeAsHidden(bitmaps, scale));
                })
                .show();

        merger.sbScaleToBlack = merger.dialog.findViewById(R.id.sb_scale_to_black);
        merger.sbScaleToWhite = merger.dialog.findViewById(R.id.sb_scale_to_white);
    }

    private static Bitmap mergeAsHidden(@Size(2) Bitmap[] bitmaps, @Size(2) float[] scale) {
        final int[] width = {bitmaps[0].getWidth(), bitmaps[1].getWidth()},
                height = {bitmaps[0].getHeight(), bitmaps[1].getHeight()};
        final int w = Math.max(width[0], width[1]), h = Math.max(height[0], height[1]), area = w * h;
        final int[] left = {0, 0}, top = {0, 0};
        {
            final int iaw = width[0] >= width[1] ? 0 : 1, iiw = 1 - iaw, // iaw - Index of max width. i - Min.
                    iah = height[0] >= width[1] ? 0 : 1, iih = 1 - iah; // h - Height.
            left[iiw] = width[iaw] - width[iiw] >> 1;
            top[iih] = height[iah] - height[iih] >> 1;
        }
        final Bitmap[] bitmaps_ = {
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        };
        {
            final Canvas[] canvases = {new Canvas(bitmaps_[0]), new Canvas(bitmaps_[1])};
            final Paint paint = new Paint();
            final float shift0 = (1.0f - scale[0]) * 0xFF;

            canvases[0].drawColor(Color.WHITE, BlendMode.DST_OVER);
            paint.setColorFilter(new ColorMatrixColorFilter(new float[]{
                    scale[0], 0.0f, 0.0f, 0.0f, shift0,
                    0.0f, scale[0], 0.0f, 0.0f, shift0,
                    0.0f, 0.0f, scale[0], 0.0f, shift0,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            }));
            canvases[0].drawBitmap(bitmaps[0], left[0], top[0], paint);

            canvases[1].drawColor(Color.BLACK, BlendMode.DST_OVER);
            paint.setColorFilter(new ColorMatrixColorFilter(new float[]{
                    scale[1], 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, scale[1], 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, scale[1], 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            }));
            canvases[1].drawBitmap(bitmaps[1], left[1], top[1], paint);
        }
        final int[][] pixels = {new int[area], new int[area]};
        final int[] pixels_ = new int[area];
        bitmaps_[0].getPixels(pixels[0], 0, w, 0, 0, w, h);
        bitmaps_[1].getPixels(pixels[1], 0, w, 0, 0, w, h);

        bitmaps_[0].recycle();
        bitmaps_[1].recycle();

        for (int i = 0; i < area; ++i) {
            final float[] red = {Color.red(pixels[0][i]) / 255.0f, Color.red(pixels[1][i]) / 255.0f},
                    green = {Color.green(pixels[0][i]) / 255.0f, Color.green(pixels[1][i]) / 255.0f},
                    blue = {Color.blue(pixels[0][i]) / 255.0f, Color.blue(pixels[1][i]) / 255.0f};
            final float[] average = {(red[0] + green[0] + blue[0]) / 3.0f, (red[1] + green[1] + blue[1]) / 3.0f};
            final float a = saturate(1 + (average[1] - average[0]));
            final float ar = saturate(1 + (red[1] - red[0])),
                    ag = saturate(1 + (green[1] - green[0])),
                    ab = saturate(1 + (blue[1] - blue[0]));
            pixels_[i] = Color.argb(a,
                    saturate(ar > 0.0f ? (red[1] / ar) : 1.0f),
                    saturate(ag > 0.0f ? (green[1] / ag) : 1.0f),
                    saturate(ab > 0.0f ? (blue[1] / ab) : 1.0f));
        }

        final Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bm.setPixels(pixels_, 0, w, 0, 0, w, h);
        return bm;
    }

    private static float saturate(float a) {
        return Math.max(Math.min(a, 1.0f), 0.0f);
    }
}
