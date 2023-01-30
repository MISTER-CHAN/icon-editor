package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

class ImageSizeManager {

    public interface OnUpdateListener {
        void onUpdate(int width, int height, boolean stretch);
    }

    private AlertDialog.Builder builder;
    private Bitmap bitmap;
    private EditText etSizeX, etSizeY;
    private OnUpdateListener listener;
    private RadioButton rbStretch;

    private AfterTextChangedListener onSizeXTextChangedListener, onSizeYTextChangedListener;

    public static ImageSizeManager make(Context context, Bitmap bitmap, OnUpdateListener listener) {
        final ImageSizeManager manager = new ImageSizeManager();

        manager.bitmap = bitmap;
        manager.listener = listener;

        manager.builder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.image_size)
                .setView(R.layout.image_size);

        manager.builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            try {
                int width = Integer.parseUnsignedInt(manager.etSizeX.getText().toString());
                int height = Integer.parseUnsignedInt(manager.etSizeY.getText().toString());
                boolean stretch = manager.rbStretch.isChecked();
                manager.listener.onUpdate(width, height, stretch);
            } catch (NumberFormatException e) {
            }
        });

        manager.onSizeXTextChangedListener = s ->
                manager.keepAspectRatio(s,
                        manager.etSizeY,
                        (double) bitmap.getHeight() / (double) bitmap.getWidth(),
                        manager.onSizeYTextChangedListener);

        manager.onSizeYTextChangedListener = s ->
                manager.keepAspectRatio(s,
                        manager.etSizeX,
                        (double) bitmap.getWidth() / (double) bitmap.getHeight(),
                        manager.onSizeXTextChangedListener);

        return manager;
    }

    private void keepAspectRatio(String s, EditText et, double ratio, AfterTextChangedListener listener) {
        try {
            final int i = Integer.parseUnsignedInt(s);
            et.removeTextChangedListener(listener);
            et.setText(String.valueOf((int) (i * ratio)));
            et.addTextChangedListener(listener);
        } catch (NumberFormatException e) {
        }
    }

    public void show() {

        final AlertDialog dialog = builder.show();

        etSizeX = dialog.findViewById(R.id.et_img_size_x);
        etSizeY = dialog.findViewById(R.id.et_img_size_y);
        rbStretch = dialog.findViewById(R.id.rb_img_stretch);

        etSizeX.setText(String.valueOf(bitmap.getWidth()));
        etSizeY.setText(String.valueOf(bitmap.getHeight()));
        rbStretch.setChecked(true);

        ((CompoundButton) dialog.findViewById(R.id.cb_img_lar)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etSizeX.addTextChangedListener(onSizeXTextChangedListener);
                etSizeY.addTextChangedListener(onSizeYTextChangedListener);
            } else {
                etSizeX.removeTextChangedListener(onSizeXTextChangedListener);
                etSizeY.removeTextChangedListener(onSizeYTextChangedListener);
            }
        });
    }
}
