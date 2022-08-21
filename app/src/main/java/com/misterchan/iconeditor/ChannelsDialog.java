package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class ChannelsDialog {

    private final AlertDialog.Builder builder;
    private ColorMatrixManager.OnMatrixElementsChangeListener onMatrixElementsChangeListener;

    private float[] a = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    public ChannelsDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.channels)
                .setView(R.layout.channels);
    }

    public ChannelsDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel,
                (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public ChannelsDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public ChannelsDialog setOnMatrixChangeListener(ColorMatrixManager.OnMatrixElementsChangeListener listener) {
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

        ((SeekBar) dialog.findViewById(R.id.sb_red)).setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            a[0] = progress / 10.0f;
            onMatrixElementsChangeListener.onChanged(a);
        });
        ((SeekBar) dialog.findViewById(R.id.sb_green)).setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            a[6] = progress / 10.0f;
            onMatrixElementsChangeListener.onChanged(a);
        });
        ((SeekBar) dialog.findViewById(R.id.sb_blue)).setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            a[12] = progress / 10.0f;
            onMatrixElementsChangeListener.onChanged(a);
        });
        ((SeekBar) dialog.findViewById(R.id.sb_alpha)).setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            a[18] = progress / 10.0f;
            onMatrixElementsChangeListener.onChanged(a);
        });
    }
}
