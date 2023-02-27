package com.misterchan.iconeditor;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

class NewImageDialog {

    public interface OnApplyListener {
        void onApply(int width, int height);
    }

    private final AlertDialog.Builder builder;
    private TextInputEditText tietWidth, tietHeight;

    public NewImageDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.new_)
                .setView(R.layout.new_image);
    }

    public NewImageDialog setOnFinishSettingListener(OnApplyListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            try {
                final int width = Integer.parseUnsignedInt(tietWidth.getText().toString());
                final int height = Integer.parseUnsignedInt(tietHeight.getText().toString());
                if (!(width > 0 && height > 0)) {
                    return;
                }
                listener.onApply(width, height);
            } catch (NumberFormatException e) {
            }
        });
        return this;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        tietWidth = dialog.findViewById(R.id.tiet_width);
        tietHeight = dialog.findViewById(R.id.tiet_height);
    }
}
