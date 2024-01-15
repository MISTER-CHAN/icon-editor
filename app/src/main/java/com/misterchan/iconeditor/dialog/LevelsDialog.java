package com.misterchan.iconeditor.dialog;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

import com.google.android.material.slider.RangeSlider;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnCircularRSChangeListener;
import com.misterchan.iconeditor.util.BitmapUtils;
import com.misterchan.iconeditor.util.LightingToLevels;

import java.util.List;
import java.util.function.Function;

public class LevelsDialog extends FilterDialog {

    public interface OnLevelsChangedListener {
        void onChanged(float inputShadows, float inputHighlights, float outputShadows, float outputHighlights, boolean stopped);
    }

    private final Activity activity;
    private Bitmap bitmap;
    private Bitmap progressBitmap;
    private Canvas progressCanvas;
    private ImageView iv;
    private ImageView ivProgress;
    private final OnLevelsChangedListener listener;
    private final Paint progressPaint = new Paint();

    @FloatRange(from = 0x00, to = 0xFF)
    private float inputShadows = 0x00, inputHighlights = 0xFF;

    @FloatRange(from = 0x00, to = 0xFF)
    private float outputShadows = 0x00, outputHighlights = 0xFF;

    @ColorInt
    private int[] srcPixels;

    public LevelsDialog(Activity activity, OnLevelsChangedListener listener) {
        super(activity);
        builder.setView(R.layout.levels);

        builder.setOnDismissListener(dialog -> {
            bitmap.recycle();
            bitmap = null;
            progressCanvas = null;
            progressBitmap.recycle();
            progressBitmap = null;
        });

        this.activity = activity;
        final Resources.Theme theme = activity.getTheme();
        final TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        progressPaint.setColor(activity.getResources().getColor(typedValue.resourceId, theme));

        this.listener = listener;
    }

    public static final Function<Integer, Integer> valueFunc =
            pixel -> Math.max(Math.max(Color.red(pixel), Color.green(pixel)), Color.blue(pixel));

    public LevelsDialog drawHistogram(@ColorInt int[] src) {
        srcPixels = src;
        return this;
    }

    private void drawProgress() {
        progressBitmap.eraseColor(Color.TRANSPARENT);
        progressCanvas.drawLine(inputShadows, 0.0f, inputShadows, 100.0f, progressPaint);
        progressCanvas.drawLine(inputHighlights, 0.0f, inputHighlights, 100.0f, progressPaint);
        ivProgress.invalidate();
    }

    @Override
    void onFilterCommit() {
        listener.onChanged(inputShadows, inputHighlights, outputShadows, outputHighlights, true);
    }

    public LevelsDialog set(float mul, float add) {
        final float[] arr = LightingToLevels.lightingToLevels(mul, add);
        return set(arr[0], arr[1], arr[2], arr[3]);
    }

    public LevelsDialog set(float inputShadows, float inputHighlights, float outputShadows, float outputHighlights) {
        this.inputShadows = (int) inputShadows;
        this.inputHighlights = (int) inputHighlights;
        this.outputShadows = (int) outputShadows;
        this.outputHighlights = (int) outputHighlights;
        return this;
    }

    @Override
    public void show() {
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

        if (srcPixels != null) {
            new Thread(() -> {
                BitmapUtils.drawHistogram(srcPixels, bitmap, 4);
                activity.runOnUiThread(() -> iv.invalidate());
                srcPixels = null;
            }).start();
        }
    }
}
