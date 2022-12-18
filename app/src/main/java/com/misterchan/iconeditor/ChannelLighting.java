package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

public class ChannelLighting {

    private final AlertDialog.Builder builder;
    private ColorMatrixManager.OnMatrixElementsChangeListener onMatrixElementsChangeListener;

    @Size(20)
    private final float[] a = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    public ChannelLighting(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.channel_lighting)
                .setView(R.layout.channel_lighting);
    }

    private void setElement(int index, float e) {
        a[index] = e;
        onMatrixElementsChangeListener.onChanged(a);
    }

    public ChannelLighting setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel,
                (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public ChannelLighting setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public ChannelLighting setOnMatrixChangeListener(ColorMatrixManager.OnMatrixElementsChangeListener listener) {
        onMatrixElementsChangeListener = listener;
        return this;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        ((SeekBar) dialog.findViewById(R.id.sb_red_scale)).setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> setElement(0, progress / 10.0f));
        ((SeekBar) dialog.findViewById(R.id.sb_red_shift)).setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> setElement(4, progress));
        ((SeekBar) dialog.findViewById(R.id.sb_green_scale)).setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> setElement(6, progress / 10.0f));
        ((SeekBar) dialog.findViewById(R.id.sb_green_shift)).setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> setElement(9, progress));
        ((SeekBar) dialog.findViewById(R.id.sb_blue_scale)).setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> setElement(12, progress / 10.0f));
        ((SeekBar) dialog.findViewById(R.id.sb_blue_shift)).setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> setElement(14, progress));
        ((SeekBar) dialog.findViewById(R.id.sb_alpha_scale)).setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> setElement(18, progress / 10.0f));
        ((SeekBar) dialog.findViewById(R.id.sb_alpha_shift)).setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> setElement(19, progress));
    }
}
