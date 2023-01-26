package com.misterchan.iconeditor;

import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

public class ArgbColorIntPicker extends ArgbColorPicker {

    private int radix = 16;
    private String format;

    private void loadColor(@ColorInt int color) {
        tietRed.setText(String.format(format, Color.red(color)));
        tietGreen.setText(String.format(format, Color.green(color)));
        tietBlue.setText(String.format(format, Color.blue(color)));
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

        sbAlpha.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietAlpha.setText(String.format(format, progress)));
        sbRed.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietRed.setText(String.format(format, progress)));
        sbGreen.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietGreen.setText(String.format(format, progress)));
        sbBlue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietBlue.setText(String.format(format, progress)));
        tietAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbAlpha));
        tietRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbRed));
        tietGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbGreen));
        tietBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbBlue));

        final int color = Color.toArgb(oldColor);
        tietAlpha.setText(String.format(format, Color.alpha(color)));
        loadColor(color);

        final OnColorPickListener onColorPickListener = (oldColor, newColor) -> loadColor(Color.toArgb(newColor));
        showOtherColorPickers(dialog, onColorPickListener);
    }
}
