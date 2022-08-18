package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.EditText;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import java.text.DecimalFormat;

public class ColorMatrixManager {

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("0.#######");

    private AlertDialog.Builder dialogBuilder;
    private ValueCallback<float[]> valueCallback;

    private float[] a;

    public static ColorMatrixManager make(Context context,
                                          int titleId,
                                          final ValueCallback<float[]> valueCallback,
                                          final DialogInterface.OnClickListener onPosButtonClickListener,
                                          @Size(value = 20) float[] defaultMatrix) {
        return make(context,
                titleId,
                valueCallback,
                onPosButtonClickListener,
                null,
                defaultMatrix);
    }

    public static ColorMatrixManager make(Context context,
                                          int titleId,
                                          final ValueCallback<float[]> valueCallback,
                                          final DialogInterface.OnClickListener onPosButtonClickListener,
                                          final DialogInterface.OnCancelListener onCancelListener) {
        return make(context,
                titleId,
                valueCallback,
                onPosButtonClickListener,
                onCancelListener,
                new float[]{
                        1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                });
    }

    public static ColorMatrixManager make(Context context,
                                          int titleId,
                                          final ValueCallback<float[]> valueCallback,
                                          final DialogInterface.OnClickListener onPosButtonClickListener,
                                          final DialogInterface.OnCancelListener onCancelListener,
                                          @Size(value = 20) float[] defaultMatrix) {
        ColorMatrixManager manager = new ColorMatrixManager();

        manager.a = defaultMatrix;

        manager.dialogBuilder = new AlertDialog.Builder(context)
                .setOnCancelListener(onCancelListener)
                .setPositiveButton(R.string.ok, onPosButtonClickListener)
                .setTitle(titleId)
                .setView(R.layout.color_matrix);

        if (onCancelListener != null) {
            manager.dialogBuilder.setNegativeButton(R.string.cancel,
                    (dialog, which) -> onCancelListener.onCancel(dialog));
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

        EditText[] editTexts = {
                dialog.findViewById(R.id.et_cm_a), dialog.findViewById(R.id.et_cm_b), dialog.findViewById(R.id.et_cm_c), dialog.findViewById(R.id.et_cm_d), dialog.findViewById(R.id.et_cm_e),
                dialog.findViewById(R.id.et_cm_f), dialog.findViewById(R.id.et_cm_g), dialog.findViewById(R.id.et_cm_h), dialog.findViewById(R.id.et_cm_i), dialog.findViewById(R.id.et_cm_j),
                dialog.findViewById(R.id.et_cm_k), dialog.findViewById(R.id.et_cm_l), dialog.findViewById(R.id.et_cm_m), dialog.findViewById(R.id.et_cm_n), dialog.findViewById(R.id.et_cm_o),
                dialog.findViewById(R.id.et_cm_p), dialog.findViewById(R.id.et_cm_q), dialog.findViewById(R.id.et_cm_r), dialog.findViewById(R.id.et_cm_s), dialog.findViewById(R.id.et_cm_t),
        };

        for (int i = 0; i < editTexts.length; ++i) {
            EditText et = editTexts[i];
            et.setText(DEC_FORMAT.format(a[i]));
            final int index = i;
            et.addTextChangedListener((AfterTextChangedListener) s -> tryParsing(index, s));
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
