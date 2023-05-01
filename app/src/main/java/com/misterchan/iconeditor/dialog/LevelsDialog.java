package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

import java.util.function.Function;

public class LevelsDialog {

    public interface OnLevelsChangedListener {
        void onChanged(float inputShadows, float inputHighlights, float outputShadows, float outputHighlights, boolean stopped);
    }

    private Bitmap bitmap;
    private Bitmap progressBitmap;
    private final AlertDialog.Builder builder;
    private Canvas progressCanvas;
    private ImageView iv;
    private ImageView ivProgress;
    private OnLevelsChangedListener listener;
    private final Paint paint = new Paint();
    private Slider sInputHighlights, sInputShadows;
    private Slider sOutputHighlights, sOutputShadows;

    public LevelsDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.levels)
                .setView(R.layout.levels);

        builder.setOnDismissListener(dialog -> {
            bitmap.recycle();
            bitmap = null;
            progressCanvas = null;
            progressBitmap.recycle();
            progressBitmap = null;
        });

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        paint.setColor(typedArray.getColor(0, Color.BLACK));
        typedArray.recycle();
    }

    public static final Function<Integer, Integer> valueFunc =
            pixel -> Math.max(Math.max(Color.red(pixel), Color.green(pixel)), Color.blue(pixel));

    public void drawHistogram(int[] src) {
        drawHistogram(src, bitmap, iv, valueFunc, 100.0f, paint);
    }

    public static void drawHistogram(int[] src, Bitmap dst, ImageView iv,
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
                cv.drawRect(i,
                        maxHeight - (float) numValue[i] / (float) max * maxHeight,
                        ++i,
                        maxHeight,
                        paint);
            }
            iv.invalidate();
        }).start();
    }

    private void drawProgress() {
        progressBitmap.eraseColor(Color.TRANSPARENT);
        progressCanvas.drawLine(sInputShadows.getValue(), 0.0f, sInputShadows.getValue(), 100.0f, paint);
        progressCanvas.drawLine(sInputHighlights.getValue(), 0.0f, sInputHighlights.getValue(), 100.0f, paint);
        ivProgress.invalidate();
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
        sInputHighlights = dialog.findViewById(R.id.s_input_highlights);
        sInputShadows = dialog.findViewById(R.id.s_input_shadows);
        sOutputHighlights = dialog.findViewById(R.id.s_output_highlights);
        sOutputShadows = dialog.findViewById(R.id.s_output_shadows);

        final OnSliderChangeListener l = (slider, value, stopped) -> update(stopped);

        sInputHighlights.addOnSliderTouchListener(l);
        sInputHighlights.addOnChangeListener(l);
        sInputShadows.addOnSliderTouchListener(l);
        sInputShadows.addOnChangeListener(l);
        sOutputHighlights.addOnSliderTouchListener(l);
        sOutputHighlights.addOnChangeListener(l);
        sOutputShadows.addOnSliderTouchListener(l);
        sOutputShadows.addOnChangeListener(l);

        bitmap = Bitmap.createBitmap(0x100, 100, Bitmap.Config.ARGB_4444);
        iv.setImageBitmap(bitmap);

        progressBitmap = Bitmap.createBitmap(0x100, 100, Bitmap.Config.ARGB_4444);
        progressCanvas = new Canvas(progressBitmap);
        ivProgress.setImageBitmap(progressBitmap);

        drawProgress();

        return this;
    }

    private void update(boolean stopped) {
        drawProgress();
        listener.onChanged(sInputShadows.getValue(), sInputHighlights.getValue(),
                sOutputShadows.getValue(), sOutputHighlights.getValue(),
                stopped);
    }
}
