package com.misterchan.iconeditor;

import android.content.Context;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class NewGraphicPropertiesDialog {

    public interface OnFinishSettingListener {
        void onFinish(int width, int height);
    }

    private final AlertDialog.Builder builder;
    private EditText etSizeX, etSizeY;

    public NewGraphicPropertiesDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.new_)
                .setView(R.layout.new_graphic);
    }

    public NewGraphicPropertiesDialog setOnFinishSettingListener(OnFinishSettingListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            try {
                int width = Integer.parseUnsignedInt(etSizeX.getText().toString());
                int height = Integer.parseUnsignedInt(etSizeY.getText().toString());
                listener.onFinish(width, height);
            } catch (NumberFormatException e) {
            }
        });
        return this;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        etSizeX = dialog.findViewById(R.id.et_size_x);
        etSizeY = dialog.findViewById(R.id.et_size_y);
    }
}
