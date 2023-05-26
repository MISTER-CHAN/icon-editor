package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.R;

public class ImageSizeManager {

    public enum ScaleType {
        STRETCH, STRETCH_FILTER, CROP;
    }

    public interface OnApplyListener {
        void onApply(int width, int height, ScaleType transform);
    }

    private final AlertDialog.Builder builder;
    private CheckBox cbFilter;
    private final double ratio;
    private final int defaultWidth, defaultHeight;
    private RadioButton rbStretch;
    private TextInputEditText tietWidth, tietHeight;

    private final AfterTextChangedListener onSizeXTextChangedListener = this::onWidthTextChanged;

    private final AfterTextChangedListener onSizeYTextChangedListener = this::onHeightTextChanged;

    public ImageSizeManager(Context context, Bitmap bitmap, OnApplyListener listener) {
        defaultWidth = bitmap.getWidth();
        defaultHeight = bitmap.getHeight();
        ratio = (double) defaultWidth / (double) defaultHeight;

        builder = new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_photo_size_select_large)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.image_size)
                .setView(R.layout.image_size);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            try {
                final int width = Integer.parseUnsignedInt(tietWidth.getText().toString());
                final int height = Integer.parseUnsignedInt(tietHeight.getText().toString());
                if (!(width > 0 && height > 0)) {
                    return;
                }
                final ScaleType scaleType = rbStretch.isChecked()
                        ? cbFilter.isChecked() ? ScaleType.STRETCH_FILTER : ScaleType.STRETCH
                        : ScaleType.CROP;
                listener.onApply(width, height, scaleType);
            } catch (NumberFormatException e) {
            }
        });
    }

    private void onWidthTextChanged(String s) {
        try {
            final int i = Integer.parseUnsignedInt(s);
            tietHeight.removeTextChangedListener(onSizeYTextChangedListener);
            tietHeight.setText(String.valueOf((int) (i / ratio)));
            tietHeight.addTextChangedListener(onSizeYTextChangedListener);
        } catch (NumberFormatException e) {
        }
    }

    private void onHeightTextChanged(String s) {
        try {
            final int i = Integer.parseUnsignedInt(s);
            tietWidth.removeTextChangedListener(onSizeXTextChangedListener);
            tietWidth.setText(String.valueOf((int) (i * ratio)));
            tietWidth.addTextChangedListener(onSizeXTextChangedListener);
        } catch (NumberFormatException e) {
        }
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        cbFilter = dialog.findViewById(R.id.cb_filter);
        tietWidth = dialog.findViewById(R.id.tiet_width);
        tietHeight = dialog.findViewById(R.id.tiet_height);
        rbStretch = dialog.findViewById(R.id.rb_stretch);

        cbFilter.setChecked(true);
        tietWidth.setText(String.valueOf(defaultWidth));
        tietHeight.setText(String.valueOf(defaultHeight));
        rbStretch.setOnCheckedChangeListener((buttonView, isChecked) -> cbFilter.setEnabled(isChecked));
        rbStretch.setChecked(true);

        ((CompoundButton) dialog.findViewById(R.id.cb_lar)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tietWidth.addTextChangedListener(onSizeXTextChangedListener);
                tietHeight.addTextChangedListener(onSizeYTextChangedListener);
            } else {
                tietWidth.removeTextChangedListener(onSizeXTextChangedListener);
                tietHeight.removeTextChangedListener(onSizeYTextChangedListener);
            }
        });
    }
}
