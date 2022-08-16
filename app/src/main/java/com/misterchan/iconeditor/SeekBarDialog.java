package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class SeekBarDialog {

    private AlertDialog.Builder dialogBuilder;
    private int max, min, progress;
    private OnProgressChangeListener onProgressChangeListener;

    public static SeekBarDialog make(Context context, int titleId, int min, int max, int progress,
                                     final OnProgressChangeListener onProgressChangeListener,
                                     final DialogInterface.OnClickListener onPosButtonClickListener,
                                     final DialogInterface.OnCancelListener onCancelListener) {

        SeekBarDialog seekBarDialog = new SeekBarDialog();

        seekBarDialog.dialogBuilder = new AlertDialog.Builder(context)
                .setOnCancelListener(onCancelListener)
                .setNegativeButton(R.string.cancel, (dialog, which) -> onCancelListener.onCancel(dialog))
                .setPositiveButton(R.string.ok, onPosButtonClickListener)
                .setTitle(titleId)
                .setView(R.layout.seek_bar);

        seekBarDialog.max = max;
        seekBarDialog.min = min;
        seekBarDialog.progress = progress;
        seekBarDialog.onProgressChangeListener = onProgressChangeListener;

        return seekBarDialog;
    }

    public void show() {
        AlertDialog dialog = dialogBuilder.show();

        android.view.Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        SeekBar seekBar = dialog.findViewById(R.id.seek_bar);
        seekBar.setMax(max);
        seekBar.setMin(min);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(onProgressChangeListener);
    }
}
