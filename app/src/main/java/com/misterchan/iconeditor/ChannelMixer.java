package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class ChannelMixer {

    private final AlertDialog.Builder builder;
    private ColorMatrixManager.OnMatrixElementsChangeListener onMatrixElementsChangeListener;

    private final float[] a = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    public ChannelMixer(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.channels)
                .setView(R.layout.channels);
    }

    private void setElement(int index, float e) {
        a[index] = e;
        onMatrixElementsChangeListener.onChanged(a);
    }

    public ChannelMixer setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel,
                (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public ChannelMixer setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public ChannelMixer setOnMatrixChangeListener(ColorMatrixManager.OnMatrixElementsChangeListener listener) {
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

        ((SeekBar) dialog.findViewById(R.id.sb_red_scale)).setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> setElement(0, progress / 10.0f));
        ((SeekBar) dialog.findViewById(R.id.sb_red_shift)).setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> setElement(4, progress));
        ((SeekBar) dialog.findViewById(R.id.sb_green_scale)).setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> setElement(6, progress / 10.0f));
        ((SeekBar) dialog.findViewById(R.id.sb_green_shift)).setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> setElement(9, progress));
        ((SeekBar) dialog.findViewById(R.id.sb_blue_scale)).setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> setElement(12, progress / 10.0f));
        ((SeekBar) dialog.findViewById(R.id.sb_blue_shift)).setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> setElement(14, progress));
        ((SeekBar) dialog.findViewById(R.id.sb_alpha_scale)).setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> setElement(18, progress / 10.0f));
        ((SeekBar) dialog.findViewById(R.id.sb_alpha_shift)).setOnSeekBarChangeListener((OnProgressChangeListener) (seekBar, progress) -> setElement(19, progress));
    }
}
