package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.R;

import java.text.DecimalFormat;

public class ColorMatrixManager {

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("0.#######");

    public interface OnMatrixElementsChangedListener {
        void onChanged(float[] matrix);
    }

    private final AlertDialog.Builder dialogBuilder;
    private final OnMatrixElementsChangedListener onMatrixElementsChangeListener;

    @Size(20)
    private final float[] m;

    public ColorMatrixManager(Context context,
                                          final OnMatrixElementsChangedListener onMatrixElementsChangeListener,
                                          @Size(value = 20) float[] defaultMatrix) {
        this(context,
                onMatrixElementsChangeListener,
                null, null,
                defaultMatrix);
    }

    public ColorMatrixManager(Context context,
                                          final OnMatrixElementsChangedListener onMatrixElementsChangeListener,
                                          final DialogInterface.OnClickListener onPosButtonClickListener,
                                          final DialogInterface.OnCancelListener onCancelListener) {
        this(context,
                onMatrixElementsChangeListener,
                onPosButtonClickListener,
                onCancelListener,
                new float[]{
                        1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                });
    }

    public ColorMatrixManager(Context context,
                                          final OnMatrixElementsChangedListener onMatrixElementsChangeListener,
                                          final DialogInterface.OnClickListener onPosButtonClickListener,
                                          final DialogInterface.OnCancelListener onCancelListener,
                                          @Size(value = 20) float[] defaultMatrix) {
        m = defaultMatrix;

        dialogBuilder = new AlertDialog.Builder(context)
                .setOnCancelListener(onCancelListener)
                .setPositiveButton(R.string.ok, onPosButtonClickListener)
                .setTitle(R.string.channel_mixer)
                .setView(R.layout.color_matrix);

        if (onCancelListener != null) {
            dialogBuilder.setNegativeButton(R.string.cancel,
                    (dialog, which) -> onCancelListener.onCancel(dialog));
        }

        this.onMatrixElementsChangeListener = onMatrixElementsChangeListener;
    }

    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final GridLayout gl = dialog.findViewById(R.id.gl);

        for (int i = 0; i < 20; ++i) {
            TextInputEditText tiet = (TextInputEditText) gl.getChildAt(i);
            tiet.setText(DEC_FORMAT.format(m[i]));
            final int index = i;
            tiet.addTextChangedListener((AfterTextChangedListener) s -> tryParsing(index, s));
        }
    }

    private void tryParsing(int index, String s) {
        try {
            final float f = Float.parseFloat(s);
            m[index] = f;
        } catch (NumberFormatException e) {
        }
        onMatrixElementsChangeListener.onChanged(m);
    }
}
