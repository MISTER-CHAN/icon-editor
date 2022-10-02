package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class LevelsDialog {

    public interface OnLevelsChangeListener {
        void onChange(int shadows, int highlights);
    }

    private Bitmap bitmap;
    private Bitmap progressBitmap;
    private final AlertDialog.Builder builder;
    private Canvas progressCanvas;
    private ImageView iv;
    private ImageView ivProgress;
    private OnLevelsChangeListener listener;
    private final Paint paint = new Paint();
    private SeekBar sbHighlights, sbShadows;

    public LevelsDialog(Context context) {
        DialogInterface.OnDismissListener onDismissListener = dialog -> {
            bitmap.recycle();
            bitmap = null;
            progressCanvas = null;
            progressBitmap.recycle();
            progressBitmap = null;
        };
        builder = new AlertDialog.Builder(context)
                .setOnDismissListener(onDismissListener)
                .setTitle(R.string.levels)
                .setView(R.layout.levels);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        paint.setColor(typedArray.getColor(0, 0xFF000000));
        typedArray.recycle();
    }

    private void drawProgress() {
        progressCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        progressCanvas.drawLine(sbShadows.getProgress(), 0.0f, sbShadows.getProgress(), 100.0f, paint);
        progressCanvas.drawLine(sbHighlights.getProgress(), 0.0f, sbHighlights.getProgress(), 100.0f, paint);
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

    public LevelsDialog setOnLevelsChangeListener(OnLevelsChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public LevelsDialog show() {
        AlertDialog dialog = builder.show();

        android.view.Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        iv = dialog.findViewById(R.id.iv);
        ivProgress = dialog.findViewById(R.id.iv_progress);
        sbHighlights = dialog.findViewById(R.id.sb_highlights);
        sbShadows = dialog.findViewById(R.id.sb_shadows);

        sbHighlights.setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> update());
        sbShadows.setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> update());

        bitmap = Bitmap.createBitmap(0x100, 100, Bitmap.Config.ARGB_4444);
        iv.setImageBitmap(bitmap);

        progressBitmap = Bitmap.createBitmap(0x100, 100, Bitmap.Config.ARGB_4444);
        progressCanvas = new Canvas(progressBitmap);
        ivProgress.setImageBitmap(progressBitmap);

        drawProgress();

        return this;
    }

    private void update() {
        listener.onChange(sbShadows.getProgress(), sbHighlights.getProgress());
        drawProgress();
    }

    public void updateImage(Bitmap bitmap) {
        Canvas cv = new Canvas(this.bitmap);
        final int w = bitmap.getWidth(), h = bitmap.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        final int[] numValue = new int[0x100];
        int max = 1;
        for (int i = 0; i < area; ++i) {
            int pixel = pixels[i];
            int r = Color.red(pixel), g = Color.green(pixel), b = Color.blue(pixel);
            int n = ++numValue[Math.max(Math.max(r, g), b)];
            if (n > max) {
                max = n;
            }
        }
        for (int i = 0x0; i < 0x100; ) {
            cv.drawRect(i,
                    100.0f - (float) numValue[i] / (float) max * 100.0f,
                    ++i,
                    100.0f,
                    paint);
        }
        iv.invalidate();
    }
}
