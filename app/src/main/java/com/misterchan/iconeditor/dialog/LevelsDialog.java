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
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.listener.OnSeekBarChangeListener;
import com.misterchan.iconeditor.R;

import java.util.function.Function;

public class LevelsDialog {

    public interface OnLevelsChangedListener {
        void onChanged(int inputShadows, int inputHighlights, int outputShadows, int outputHighlights, boolean stopped);
    }

    private Bitmap bitmap;
    private Bitmap progressBitmap;
    private final AlertDialog.Builder builder;
    private Canvas progressCanvas;
    private ImageView iv;
    private ImageView ivProgress;
    private OnLevelsChangedListener listener;
    private final Paint paint = new Paint();
    private SeekBar sbInputHighlights, sbInputShadows;
    private SeekBar sbOutputHighlights, sbOutputShadows;

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
        progressCanvas.drawLine(sbInputShadows.getProgress(), 0.0f, sbInputShadows.getProgress(), 100.0f, paint);
        progressCanvas.drawLine(sbInputHighlights.getProgress(), 0.0f, sbInputHighlights.getProgress(), 100.0f, paint);
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
        sbInputHighlights = dialog.findViewById(R.id.sb_input_highlights);
        sbInputShadows = dialog.findViewById(R.id.sb_input_shadows);
        sbOutputHighlights = dialog.findViewById(R.id.sb_output_highlights);
        sbOutputShadows = dialog.findViewById(R.id.sb_output_shadows);

        final OnSeekBarChangeListener l = (progress, stopped) -> update(stopped);

        sbInputHighlights.setOnSeekBarChangeListener(l);
        sbInputShadows.setOnSeekBarChangeListener(l);
        sbOutputHighlights.setOnSeekBarChangeListener(l);
        sbOutputShadows.setOnSeekBarChangeListener(l);

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
        listener.onChanged(sbInputShadows.getProgress(), sbInputHighlights.getProgress(),
                sbOutputShadows.getProgress(), sbOutputHighlights.getProgress(),
                stopped);
    }
}