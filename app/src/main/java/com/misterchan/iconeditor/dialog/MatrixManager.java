package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.Matrix;
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

public class MatrixManager extends FilterDialog {

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("0.#######");

    public interface OnMatrixElementsChangedListener {
        void onChanged(Matrix matrix);
    }

    private final Matrix matrix;
    private final OnMatrixElementsChangedListener listener;

    @Size(9)
    private final float[] values = new float[9];

    public MatrixManager(Context context,
                         OnMatrixElementsChangedListener listener) {
        this(context, listener, new Matrix());
    }

    public MatrixManager(Context context,
                         OnMatrixElementsChangedListener onMatrixElementsChangeListener,
                         Matrix defaultMatrix) {
        super(context);
        builder.setView(R.layout.matrix);

        ((MaterialAlertDialogBuilder) builder).setBackgroundInsetBottom(0);

        matrix = defaultMatrix;
        matrix.getValues(values);
        this.listener = onMatrixElementsChangeListener;
    }

    @Override
    void onFilterCommit() {
        listener.onChanged(matrix);
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

        for (int i = 0; i < 9; ++i) {
            TextInputEditText tiet = (TextInputEditText) gl.getChildAt(i);
            tiet.setText(DEC_FORMAT.format(values[i]));
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
        values[index] = f;
        matrix.setValues(values);
        listener.onChanged(matrix);
    }
}
