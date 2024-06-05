package com.misterchan.iconeditor.dialog;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;

public class NewImageDialog {

    public interface OnApplyListener {
        void onApply(int width, int height);
    }

    private final AlertDialog.Builder builder;
    private TextInputEditText tietWidth, tietHeight;

    public NewImageDialog(Context context) {
        builder = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.new_)
                .setView(R.layout.new_image);
    }

    public NewImageDialog setOnFinishSettingListener(OnApplyListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            final int width, height;
            try {
                width = Integer.parseUnsignedInt(tietWidth.getText().toString());
                height = Integer.parseUnsignedInt(tietHeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            if (!(width > 0 && height > 0)) {
                return;
            }
            listener.onApply(width, height);
        });
        return this;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        tietWidth = dialog.findViewById(R.id.tiet_width);
        tietHeight = dialog.findViewById(R.id.tiet_height);

        tietWidth.setText(String.valueOf(Settings.INST.newImageWidth()));
        tietHeight.setText(String.valueOf(Settings.INST.newImageHeight()));
    }
}
