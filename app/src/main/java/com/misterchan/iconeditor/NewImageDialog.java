package com.misterchan.iconeditor;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

class NewImageDialog {

    public interface OnFinishSettingListener {
        void onFinish(int width, int height);
    }

    private final AlertDialog.Builder builder;
    private TextInputEditText tietSizeX, tietSizeY;

    public NewImageDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.new_)
                .setView(R.layout.new_image);
    }

    public NewImageDialog setOnFinishSettingListener(OnFinishSettingListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            try {
                int width = Integer.parseUnsignedInt(tietSizeX.getText().toString());
                int height = Integer.parseUnsignedInt(tietSizeY.getText().toString());
                listener.onFinish(width, height);
            } catch (NumberFormatException e) {
            }
        });
        return this;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        tietSizeX = dialog.findViewById(R.id.tiet_size_x);
        tietSizeY = dialog.findViewById(R.id.tiet_size_y);
    }
}
