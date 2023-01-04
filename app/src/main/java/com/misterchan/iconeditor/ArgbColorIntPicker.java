package com.misterchan.iconeditor;

import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

public class ArgbColorIntPicker extends ArgbColorPicker {

    private int radix = 16;
    private String format;

    private void loadColor(@ColorInt int color) {
        etRed.setText(String.format(format, Color.red(color)));
        etGreen.setText(String.format(format, Color.green(color)));
        etBlue.setText(String.format(format, Color.blue(color)));
    }

    @Override
    public void make(Settings settings) {
        format = settings.getArgbComponentFormat();
        radix = settings.getArgbComponentRadix();
    }

    protected void onComponentChanged(String s, SeekBar seekBar) {
        try {
            seekBar.setProgress(Integer.parseUnsignedInt(s, radix));
        } catch (NumberFormatException e) {
        }
        final int color = Color.argb(sbAlpha.getProgress(),
                sbRed.getProgress(), sbGreen.getProgress(), sbBlue.getProgress());
        newColor = Color.pack(color);
        vPreview.setBackgroundColor(color);
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        initViews(dialog);

        sbAlpha.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etAlpha.setText(String.format(format, progress)));
        sbRed.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etRed.setText(String.format(format, progress)));
        sbGreen.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etGreen.setText(String.format(format, progress)));
        sbBlue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etBlue.setText(String.format(format, progress)));
        etAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbAlpha));
        etRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbRed));
        etGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbGreen));
        etBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbBlue));

        final int color = Color.toArgb(oldColor);
        etAlpha.setText(String.format(format, Color.alpha(color)));
        loadColor(color);

        final OnColorPickListener onColorPickListener = (oldColor, newColor) -> loadColor(Color.toArgb(newColor));
        showOtherColorPickers(dialog, onColorPickListener);
    }
}
