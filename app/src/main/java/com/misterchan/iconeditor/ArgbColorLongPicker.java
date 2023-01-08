package com.misterchan.iconeditor;

import android.graphics.ColorSpace;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

public class ArgbColorLongPicker extends ArgbColorPicker {

    private ColorSpace colorSpace;

    @Size(4)
    private final float[] argb = new float[4];

    private void loadColor(@ColorLong long color) {
        color = Color.convert(color, colorSpace);
        etRed.setText(String.valueOf(argb[1] = Color.red(color)));
        etGreen.setText(String.valueOf(argb[2] = Color.green(color)));
        etBlue.setText(String.valueOf(argb[3] = Color.blue(color)));
    }

    protected void onComponentChanged(@IntRange(from = 0, to = 3) int index, String s, SeekBar seekBar) {
        try {
            final float f = Float.parseFloat(s);
            seekBar.setProgress((int) (f * 100.0f));
            argb[index] = f;
        } catch (NumberFormatException e) {
        }
        newColor = Color.pack(argb[1], argb[2], argb[3], argb[0]);
        vPreview.setBackgroundColor(Color.toArgb(newColor));
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        colorSpace = Color.colorSpace(oldColor);
        initViews(dialog);
        final TextView tvColorSpace = dialog.findViewById(R.id.tv_color_space);

        sbAlpha.setMin((int) (colorSpace.getMinValue(3) * 100.0f));
        sbAlpha.setMax((int) (colorSpace.getMaxValue(3) * 100.0f));
        sbRed.setMin((int) (colorSpace.getMinValue(0) * 100.0f));
        sbRed.setMax((int) (colorSpace.getMaxValue(0) * 100.0f));
        sbGreen.setMin((int) (colorSpace.getMinValue(1) * 100.0f));
        sbGreen.setMax((int) (colorSpace.getMaxValue(1) * 100.0f));
        sbBlue.setMin((int) (colorSpace.getMinValue(2) * 100.0f));
        sbBlue.setMax((int) (colorSpace.getMaxValue(2) * 100.0f));
        sbAlpha.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etAlpha.setText(String.valueOf(progress / 100.0f)));
        sbRed.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etRed.setText(String.valueOf(progress / 100.0f)));
        sbGreen.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etGreen.setText(String.valueOf(progress / 100.0f)));
        sbBlue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etBlue.setText(String.valueOf(progress / 100.0f)));
        etAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(0, s, sbAlpha));
        etRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(1, s, sbRed));
        etGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(2, s, sbGreen));
        etBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(3, s, sbBlue));

        etAlpha.setText(String.valueOf(Color.alpha(oldColor)));
        loadColor(oldColor);
        tvColorSpace.setText(colorSpace.toString());

        final OnColorPickListener onColorPickListener = (oldColor, newColor) -> loadColor(newColor);
        showOtherColorPickers(dialog, onColorPickListener);
    }
}
