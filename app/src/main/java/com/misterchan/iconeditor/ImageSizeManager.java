package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

class ImageSizeManager {

    public interface OnUpdateListener {
        void onUpdate(int width, int height, boolean stretch);
    }

    private final AlertDialog.Builder builder;
    private final Bitmap bitmap;
    private TextInputEditText tietSizeX, tietSizeY;
    private final OnUpdateListener listener;
    private RadioButton rbStretch;

    private final AfterTextChangedListener onSizeXTextChangedListener = this::onSizeXTextChanged;

    private final AfterTextChangedListener onSizeYTextChangedListener = this::onSizeYTextChanged;

    public ImageSizeManager(Context context, Bitmap bitmap, OnUpdateListener listener) {
        this.bitmap = bitmap;
        this.listener = listener;

        builder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.image_size)
                .setView(R.layout.image_size);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            try {
                int width = Integer.parseUnsignedInt(tietSizeX.getText().toString());
                int height = Integer.parseUnsignedInt(tietSizeY.getText().toString());
                boolean stretch = rbStretch.isChecked();
                this.listener.onUpdate(width, height, stretch);
            } catch (NumberFormatException e) {
            }
        });
    }

    private void onSizeXTextChanged(String s) {
        try {
            final int i = Integer.parseUnsignedInt(s);
            final double ratio = (double) bitmap.getWidth() / (double) bitmap.getHeight();
            tietSizeY.removeTextChangedListener(onSizeYTextChangedListener);
            tietSizeY.setText(String.valueOf((int) (i * ratio)));
            tietSizeY.addTextChangedListener(onSizeYTextChangedListener);
        } catch (NumberFormatException e) {
        }
    }

    private void onSizeYTextChanged(String s) {
        try {
            final int i = Integer.parseUnsignedInt(s);
            final double ratio = (double) bitmap.getWidth() / (double) bitmap.getHeight();
            tietSizeX.removeTextChangedListener(onSizeXTextChangedListener);
            tietSizeX.setText(String.valueOf((int) (i * ratio)));
            tietSizeX.addTextChangedListener(onSizeXTextChangedListener);
        } catch (NumberFormatException e) {
        }
    }

    public void show() {

        final AlertDialog dialog = builder.show();

        tietSizeX = dialog.findViewById(R.id.tiet_size_x);
        tietSizeY = dialog.findViewById(R.id.tiet_size_y);
        rbStretch = dialog.findViewById(R.id.rb_stretch);

        tietSizeX.setText(String.valueOf(bitmap.getWidth()));
        tietSizeY.setText(String.valueOf(bitmap.getHeight()));
        rbStretch.setChecked(true);

        ((CompoundButton) dialog.findViewById(R.id.cb_lar)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tietSizeX.addTextChangedListener(onSizeXTextChangedListener);
                tietSizeY.addTextChangedListener(onSizeYTextChangedListener);
            } else {
                tietSizeX.removeTextChangedListener(onSizeXTextChangedListener);
                tietSizeY.removeTextChangedListener(onSizeYTextChangedListener);
            }
        });
    }
}
