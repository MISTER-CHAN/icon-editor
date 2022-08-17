package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class ChannelsManager {

    private AlertDialog.Builder dialogBuilder;
    private ValueCallback<float[]> valueCallback;

    private float[] a = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    public static ChannelsManager make(Context context,
                                       final ValueCallback<float[]> valueCallback,
                                       final DialogInterface.OnClickListener onPosButtonClickListener,
                                       final DialogInterface.OnCancelListener onCancelListener) {
        ChannelsManager manager = new ChannelsManager();
        manager.dialogBuilder = new AlertDialog.Builder(context)
                .setOnCancelListener(onCancelListener)
                .setNegativeButton(R.string.cancel, (dialog, which) -> onCancelListener.onCancel(dialog))
                .setPositiveButton(R.string.ok, onPosButtonClickListener)
                .setTitle(R.string.channels)
                .setView(R.layout.channels);

        manager.valueCallback = valueCallback;

        return manager;
    }

    public void show() {
        AlertDialog dialog = dialogBuilder.show();

        android.view.Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        ((SeekBar) dialog.findViewById(R.id.sb_red)).setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            float f = progress / 10.0f;
            if (f <= 1.0f) {
                a[0] = f;
            }
            if (f >= 1.0f) {
                f -= 1.0f;
                a[1] = f;
                a[2] = f;
            }
            valueCallback.onReceiveValue(a);
        });
        ((SeekBar) dialog.findViewById(R.id.sb_green)).setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            float f = progress / 10.0f;
            if (f <= 1.0f) {
                a[6] = f;
            }
            if (f >= 1.0f) {
                f -= 1.0f;
                a[5] = f;
                a[7] = f;
            }
            valueCallback.onReceiveValue(a);
        });
        ((SeekBar) dialog.findViewById(R.id.sb_blue)).setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            float f = progress / 10.0f;
            if (f <= 1.0f) {
                a[12] = f;
            }
            if (f >= 1.0f) {
                f -= 1.0f;
                a[10] = f;
                a[11] = f;
            }
            valueCallback.onReceiveValue(a);
        });
        ((SeekBar) dialog.findViewById(R.id.sb_alpha)).setOnSeekBarChangeListener((OnProgressChangeListener) progress -> {
            float f = progress / 10.0f;
            if (f <= 1.0f) {
                a[18] = f;
            }
            if (f >= 1.0f) {
                f -= 1.0f;
                a[15] = f;
                a[16] = f;
                a[17] = f;
            }
            valueCallback.onReceiveValue(a);
        });
    }
}
