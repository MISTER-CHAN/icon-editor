package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.R;

import java.text.DecimalFormat;

public class ColorMatrixManager extends FilterDialog {

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("0.#######");

    public interface OnMatrixElementsChangedListener {
        void onChanged(float[] matrix);
    }

    private final OnMatrixElementsChangedListener listener;

    @Size(20)
    private final float[] m;

    public ColorMatrixManager(Context context,
                              OnMatrixElementsChangedListener listener) {
        this(context, new float[]{
                1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        }, listener);
    }

    public ColorMatrixManager(Context context, @Size(value = 20) float[] defaultMatrix,
                              OnMatrixElementsChangedListener listener) {
        super(context);
        builder.setView(R.layout.color_matrix);

        ((MaterialAlertDialogBuilder) builder).setBackgroundInsetBottom(0);
        m = defaultMatrix;
        this.listener = listener;
    }

    @Override
    void onFilterCommit() {
        listener.onChanged(m);
    }

    @Override
    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final GridLayout gl = dialog.findViewById(R.id.gl_k);

        for (int i = 0; i < 20; ++i) {
            TextInputEditText tiet = (TextInputEditText) gl.getChildAt(i);
            tiet.setText(DEC_FORMAT.format(m[i]));
            final int index = i;
            tiet.addTextChangedListener((AfterTextChangedListener) s -> tryParsing(index, s));
        }
    }

    private void tryParsing(int index, String s) {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        m[index] = f;
        listener.onChanged(m);
    }
}
