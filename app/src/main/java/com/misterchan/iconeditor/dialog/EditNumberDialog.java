package com.misterchan.iconeditor.dialog;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.R;

public class EditNumberDialog {

    public interface OnPositiveButtonClickListener {
        void onClick(int number);
    }

    private final AlertDialog.Builder builder;
    private TextInputEditText tiet;

    public EditNumberDialog(Context context) {
        builder = new AlertDialog.Builder(context).setView(R.layout.edit_text)
                .setNegativeButton(R.string.cancel, null);
    }

    public EditNumberDialog setOnApplyListener(OnPositiveButtonClickListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            try {
                listener.onClick(Integer.parseUnsignedInt(tiet.getText().toString()));
            } catch (NumberFormatException e) {
            }
        });
        return this;
    }

    public EditNumberDialog setTitle(@StringRes int titleId) {
        builder.setTitle(titleId);
        return this;
    }

    public void show(int defaultNumber, CharSequence suffixText) {
        final AlertDialog dialog = builder.show();

        tiet = dialog.findViewById(R.id.tiet);
        final TextInputLayout til = dialog.findViewById(R.id.til);

        tiet.setText(String.valueOf(defaultNumber));
        til.setSuffixText(suffixText);
    }
}
