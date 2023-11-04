package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
        builder = new MaterialAlertDialogBuilder(context).setView(R.layout.edit_text)
                .setNegativeButton(R.string.cancel, null);
    }

    public EditNumberDialog setIcon(@Nullable Drawable drawable) {
        builder.setIcon(drawable);
        return this;
    }

    public EditNumberDialog setIcon(@DrawableRes int iconId) {
        builder.setIcon(iconId);
        return this;
    }

    public EditNumberDialog setNeutralButton(@StringRes int textId, OnPositiveButtonClickListener listener) {
        builder.setNeutralButton(textId, (dialog, which) -> {
            try {
                listener.onClick(Integer.parseUnsignedInt(tiet.getText().toString()));
            } catch (NumberFormatException e) {
            }
        });
        return this;
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

    public void show(int defaultNumber) {
        show(defaultNumber, null);
    }

    public void show(int defaultNumber, CharSequence suffixText) {
        final AlertDialog dialog = builder.show();

        tiet = dialog.findViewById(R.id.tiet);
        final TextInputLayout til = dialog.findViewById(R.id.til);

        tiet.setText(String.valueOf(defaultNumber));

        if (suffixText != null) til.setSuffixText(suffixText);
    }
}
