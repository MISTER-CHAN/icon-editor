package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.RangeSlider;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnCircularRSChangeListener;

import java.util.List;
import java.util.function.Function;

public class LevelsDialog {

    public interface OnLevelsChangedListener {
        void onChanged(float inputShadows, float inputHighlights, float outputShadows, float outputHighlights, boolean stopped);
    }

    private static class LightingToLevels {
        private static final int IS = 0, IH = 1, OS = 2, OH = 3;
        private static final int[] IS_IH_OS_OH = {IS, IH, OS, OH}, IS_OH_OS_IH = {IS, OH, OS, IH}, OS_IH_IS_OH = {OS, IH, IS, OH}, OS_OH_IS_IH = {OS, OH, IS, IH};
        private static final int[][] IND = {OS_OH_IS_IH, OS_IH_IS_OH, OS_IH_IS_OH, OS_IH_IS_OH, IS_OH_OS_IH, IS_OH_OS_IH, IS_OH_OS_IH, IS_IH_OS_OH, OS_OH_IS_IH, OS_IH_IS_OH, IS_OH_OS_IH, IS_IH_OS_OH};
        private static final float[] V_00_00 = {0x00, 0x00}, V_00_FF = {0x00, 0xFF}, V_FF_00 = {0xFF, 0x00}, V_FF_FF = {0xFF, 0xFF};
        private static final float[][] VAL = {V_00_FF, V_00_FF, V_00_00, V_FF_FF, V_00_FF, V_00_00, V_FF_FF, V_00_FF, V_FF_00, V_FF_00, V_FF_00, V_FF_00};

        private interface Func {
            float apply(float scale, float shift, float[] arr);
        }

        private static final Func[] F = {(scale, shift, arr) -> (shift - arr[OS]) / -scale, (scale, shift, arr) -> (arr[OH] - arr[OS]) / scale + arr[IS], (scale, shift, arr) -> shift + arr[IS] * scale, (scale, shift, arr) -> (arr[IH] - arr[IS]) * scale + arr[OS]};

        private static boolean check(float[] arr) {
            for (float f : arr) if (0x00 > f || f > 0xFF) return true;
            return false;
        }

        public static float[] lightingToLevels(float scale, float shift) {
            float[] arr = new float[4];
            for (int i = 0; i == 0 || check(arr); ++i) {
                arr[IND[i][0]] = VAL[i][0];
                arr[IND[i][1]] = VAL[i][1];
                arr[IND[i][2]] = F[IND[i][2]].apply(scale, shift, arr);
                arr[IND[i][3]] = F[IND[i][3]].apply(scale, shift, arr);
            }
            return arr;
        }
    }

    private Bitmap bitmap;
    private Bitmap progressBitmap;
    private final AlertDialog.Builder builder;
    private Canvas progressCanvas;
    private ImageView iv;
    private ImageView ivProgress;
    private OnLevelsChangedListener listener;
    private final Paint paint = new Paint();

    @FloatRange(from = 0x00, to = 0xFF)
    private float inputShadows = 0x00, inputHighlights = 0xFF;

    @FloatRange(from = 0x00, to = 0xFF)
    private float outputShadows = 0x00, outputHighlights = 0xFF;

    public LevelsDialog(Context context) {
        builder = new MaterialAlertDialogBuilder(context)
                .setView(R.layout.levels);

        builder.setOnDismissListener(dialog -> {
            bitmap.recycle();
            bitmap = null;
            progressCanvas = null;
            progressBitmap.recycle();
            progressBitmap = null;
        });

        final Resources.Theme theme = context.getTheme();
        final TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        paint.setColor(context.getResources().getColor(typedValue.resourceId, theme));
    }

    public static final Function<Integer, Integer> valueFunc =
            pixel -> Math.max(Math.max(Color.red(pixel), Color.green(pixel)), Color.blue(pixel));

    public void drawHistogram(@ColorInt int[] src) {
        drawHistogram(src, bitmap, iv, valueFunc, 100.0f, paint);
    }

    public static void drawHistogram(@ColorInt int[] src, Bitmap dst, ImageView iv,
                                     Function<Integer, Integer> f, float maxHeight, Paint paint) {
        new Thread(() -> {
            final Canvas cv = new Canvas(dst);
            final int[] numValue = new int[0x100];
            int max = 1;
            for (final int pixel : src) {
                final int n = ++numValue[f.apply(pixel)];
                if (n > max) {
                    max = n;
                }
            }
            for (int i = 0x0; i < 0x100; ) {
                cv.drawRect(i, maxHeight - (float) numValue[i] / (float) max * maxHeight,
                        ++i, maxHeight, paint);
            }
            iv.invalidate();
        }).start();
    }

    private void drawProgress() {
        progressBitmap.eraseColor(Color.TRANSPARENT);
        progressCanvas.drawLine(inputShadows, 0.0f, inputShadows, 100.0f, paint);
        progressCanvas.drawLine(inputHighlights, 0.0f, inputHighlights, 100.0f, paint);
        ivProgress.invalidate();
    }

    public LevelsDialog set(float scale, float shift) {
        final float[] arr = LightingToLevels.lightingToLevels(scale, shift);
        return set(arr[0], arr[1], arr[2], arr[3]);
    }

    public LevelsDialog set(float inputShadows, float inputHighlights, float outputShadows, float outputHighlights) {
        this.inputShadows = (int) inputShadows;
        this.inputHighlights = (int) inputHighlights;
        this.outputShadows = (int) outputShadows;
        this.outputHighlights = (int) outputHighlights;
        return this;
    }

    public LevelsDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public LevelsDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public LevelsDialog setOnLevelsChangeListener(OnLevelsChangedListener listener) {
        this.listener = listener;
        return this;
    }

    public LevelsDialog show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);

        iv = dialog.findViewById(R.id.iv);
        ivProgress = dialog.findViewById(R.id.iv_progress);
        final RangeSlider rsInput = dialog.findViewById(R.id.rs_input);
        final RangeSlider rsOutput = dialog.findViewById(R.id.rs_output);

        rsInput.setValues(inputShadows, inputHighlights);
        rsOutput.setValues(outputShadows, outputHighlights);

        final OnCircularRSChangeListener inputOscl = new OnCircularRSChangeListener(false) {
            @Override
            public void onChange(@NonNull RangeSlider slider, float value, boolean inclusive, boolean stopped) {
                final List<Float> values = slider.getValues();
                inputShadows = values.get(inclusive ? 0 : 1);
                inputHighlights = values.get(inclusive ? 1 : 0);
                drawProgress();
                listener.onChanged(inputShadows, inputHighlights, outputShadows, outputHighlights, stopped);
            }
        };
        if (inputShadows > inputHighlights) {
            inputOscl.toggleInclusive(rsInput);
        }

        final OnCircularRSChangeListener outputOscl = new OnCircularRSChangeListener(false) {
            @Override
            public void onChange(@NonNull RangeSlider slider, float value, boolean inclusive, boolean stopped) {
                final List<Float> values = slider.getValues();
                outputShadows = values.get(inclusive ? 0 : 1);
                outputHighlights = values.get(inclusive ? 1 : 0);
                listener.onChanged(inputShadows, inputHighlights, outputShadows, outputHighlights, stopped);
            }
        };
        if (outputShadows > outputHighlights) {
            outputOscl.toggleInclusive(rsOutput);
        }

        rsInput.addOnChangeListener(inputOscl);
        rsInput.addOnSliderTouchListener(inputOscl);
        rsOutput.addOnChangeListener(outputOscl);
        rsOutput.addOnSliderTouchListener(outputOscl);

        bitmap = Bitmap.createBitmap(0x100, 100, Bitmap.Config.ARGB_4444);
        iv.setImageBitmap(bitmap);

        progressBitmap = Bitmap.createBitmap(0x100, 100, Bitmap.Config.ARGB_4444);
        progressCanvas = new Canvas(progressBitmap);
        ivProgress.setImageBitmap(progressBitmap);

        drawProgress();

        return this;
    }
}
