package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

import java.security.PublicKey;

public class ThresholdDialog {

    private final AlertDialog.Builder builder;
    private ColorMatrixManager.OnMatrixElementsChangeListener onMatrixElementsChangeListener;
    private SeekBar sbRed, sbGreen, sbBlue, sbThreshold;

    private float[] a = new float[]{
            0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, -0x8000,
            0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, -0x8000,
            0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, -0x8000,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    public ThresholdDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.threshold)
                .setView(R.layout.threshold);
    }

    public ThresholdDialog setDefaultMatrix(float[] matrix) {
        a = matrix;
        return this;
    }

    public ThresholdDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        return setOnCancelListener(listener, true);
    }

    public ThresholdDialog setOnCancelListener(DialogInterface.OnCancelListener listener, boolean showButton) {
        builder.setOnCancelListener(listener);
        if (showButton) {
            builder.setNegativeButton(R.string.cancel,
                    (dialog, which) -> listener.onCancel(dialog));
        }
        return this;
    }

    public ThresholdDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public ThresholdDialog setOnMatrixChangeListener(ColorMatrixManager.OnMatrixElementsChangeListener listener) {
        onMatrixElementsChangeListener = listener;
        return this;
    }

    public void show() {
        AlertDialog dialog = builder.show();

        android.view.Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        sbRed = dialog.findViewById(R.id.sb_red);
        sbGreen = dialog.findViewById(R.id.sb_green);
        sbBlue = dialog.findViewById(R.id.sb_blue);
        sbThreshold = dialog.findViewById(R.id.sb_threshold);

        sbRed.setProgress((int) (a[0] / 0x100 * 10.0f));
        sbGreen.setProgress((int) (a[1] / 0x100 * 10.0f));
        sbBlue.setProgress((int) (a[2] / 0x100 * 10.0f));
        sbThreshold.setProgress((int) (a[4] / -0x100));

        sbRed.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            updateMatrix();
            onMatrixElementsChangeListener.onChanged(a);
        });
        sbGreen.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            updateMatrix();
            onMatrixElementsChangeListener.onChanged(a);
        });
        sbBlue.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            updateMatrix();
            onMatrixElementsChangeListener.onChanged(a);
        });
        sbThreshold.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            a[4] = a[9] = a[14] = -0x100 * progress;
            onMatrixElementsChangeListener.onChanged(a);
        });

        onMatrixElementsChangeListener.onChanged(a);
    }

    private void updateMatrix() {
        float sum = Math.max(1.0f, sbRed.getProgress() + sbGreen.getProgress() + sbBlue.getProgress());
        float r = sbRed.getProgress() / sum, g = sbGreen.getProgress() / sum, b = sbBlue.getProgress() / sum;
        a[0] = a[5] = a[10] = r * 0x100;
        a[1] = a[6] = a[11] = g * 0x100;
        a[2] = a[7] = a[12] = b * 0x100;
    }
}
