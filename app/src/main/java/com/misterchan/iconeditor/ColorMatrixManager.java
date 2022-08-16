package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class ColorMatrixManager {

    private AlertDialog.Builder dialogBuilder;
    private boolean isSimple;
    private ValueCallback<float[]> valueCallback;

    private float[] a = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    public static ColorMatrixManager make(Context context, boolean isSimple,
                                          final ValueCallback<float[]> valueCallback,
                                          final DialogInterface.OnClickListener onPosButtonClickListener,
                                          final DialogInterface.OnCancelListener onCancelListener) {
        ColorMatrixManager manager = new ColorMatrixManager();
        manager.isSimple = isSimple;
        manager.dialogBuilder = new AlertDialog.Builder(context)
                .setOnCancelListener(onCancelListener)
                .setNegativeButton(R.string.cancel, (dialog, which) -> onCancelListener.onCancel(dialog))
                .setPositiveButton(R.string.ok, onPosButtonClickListener);
        if (isSimple) {
            manager.dialogBuilder.setTitle(R.string.channels)
                    .setView(R.layout.channels);
        } else {
            manager.dialogBuilder.setTitle(R.string.custom)
                    .setView(R.layout.color_matrix);
        }

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

        if (isSimple) {

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

        } else {

            ((EditText) dialog.findViewById(R.id.et_cm_a)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(0, s));
            ((EditText) dialog.findViewById(R.id.et_cm_b)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(1, s));
            ((EditText) dialog.findViewById(R.id.et_cm_c)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(2, s));
            ((EditText) dialog.findViewById(R.id.et_cm_d)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(3, s));
            ((EditText) dialog.findViewById(R.id.et_cm_e)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(4, s));
            ((EditText) dialog.findViewById(R.id.et_cm_f)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(5, s));
            ((EditText) dialog.findViewById(R.id.et_cm_g)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(6, s));
            ((EditText) dialog.findViewById(R.id.et_cm_h)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(7, s));
            ((EditText) dialog.findViewById(R.id.et_cm_i)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(8, s));
            ((EditText) dialog.findViewById(R.id.et_cm_j)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(9, s));
            ((EditText) dialog.findViewById(R.id.et_cm_k)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(10, s));
            ((EditText) dialog.findViewById(R.id.et_cm_l)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(11, s));
            ((EditText) dialog.findViewById(R.id.et_cm_m)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(12, s));
            ((EditText) dialog.findViewById(R.id.et_cm_n)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(13, s));
            ((EditText) dialog.findViewById(R.id.et_cm_o)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(14, s));
            ((EditText) dialog.findViewById(R.id.et_cm_p)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(15, s));
            ((EditText) dialog.findViewById(R.id.et_cm_q)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(16, s));
            ((EditText) dialog.findViewById(R.id.et_cm_r)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(17, s));
            ((EditText) dialog.findViewById(R.id.et_cm_s)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(18, s));
            ((EditText) dialog.findViewById(R.id.et_cm_t)).addTextChangedListener((AfterTextChangedListener) s -> tryParsing(19, s));

        }

    }

    private void tryParsing(int index, String s) {
        try {
            float f = Float.parseFloat(s);
            a[index] = f;
        } catch (NumberFormatException e) {
        }
        valueCallback.onReceiveValue(a);
    }
}
