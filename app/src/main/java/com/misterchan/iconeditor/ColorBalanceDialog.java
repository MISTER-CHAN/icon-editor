package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

public class ColorBalanceDialog {

    private final AlertDialog.Builder builder;
    private ColorMatrixManager.OnMatrixElementsChangeListener listener;
    private SeekBar sbRed, sbGreen, sbBlue;

    @Size(20)
    private final float[] matrix = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    private final OnSeekBarProgressChangeListener onProgressChangeListener = (seekBar, progress) -> {
        float r = sbRed.getProgress() / 10.0f, g = sbGreen.getProgress() / 10.0f, b = sbBlue.getProgress() / 10.0f;
        float average = (r + g + b) / 3.0f;
        matrix[0] = 1.0f + r - average;
        matrix[6] = 1.0f + g - average;
        matrix[12] = 1.0f + b - average;
        listener.onChanged(matrix);
    };

    public ColorBalanceDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.color_balance)
                .setView(R.layout.color_balance);
    }

    public ColorBalanceDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public ColorBalanceDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public ColorBalanceDialog setOnMatrixChangeListener(ColorMatrixManager.OnMatrixElementsChangeListener listener) {
        this.listener = listener;
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

        sbRed.setOnSeekBarChangeListener(onProgressChangeListener);
        sbGreen.setOnSeekBarChangeListener(onProgressChangeListener);
        sbBlue.setOnSeekBarChangeListener(onProgressChangeListener);
    }
}
