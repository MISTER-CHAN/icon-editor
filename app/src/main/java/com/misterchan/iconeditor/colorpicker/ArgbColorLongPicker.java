package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.graphics.ColorSpace;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.misterchan.iconeditor.dialog.ColorPicker;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.listener.OnSeekBarProgressChangedListener;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;

public class ArgbColorLongPicker extends ArgbColorPicker {

    private ColorSpace colorSpace;

    @Size(4)
    private final float[] argb = new float[4];

    protected ArgbColorLongPicker(Context context, int titleId, Settings settings,
                                  final ColorPicker.OnColorPickListener onColorPickListener,
                                  @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        super(context, titleId, settings, onColorPickListener, oldColor, neutralFunction);
    }

    private void loadColor(@ColorLong long color) {
        color = Color.convert(color, colorSpace);
        tietRed.setText(String.valueOf(argb[1] = Color.red(color)));
        tietGreen.setText(String.valueOf(argb[2] = Color.green(color)));
        tietBlue.setText(String.valueOf(argb[3] = Color.blue(color)));
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

        dialog.findViewById(R.id.ll_color_space).setVisibility(View.VISIBLE);
        sbAlpha.setMin((int) (colorSpace.getMinValue(3) * 100.0f));
        sbAlpha.setMax((int) (colorSpace.getMaxValue(3) * 100.0f));
        sbRed.setMin((int) (colorSpace.getMinValue(0) * 100.0f));
        sbRed.setMax((int) (colorSpace.getMaxValue(0) * 100.0f));
        sbGreen.setMin((int) (colorSpace.getMinValue(1) * 100.0f));
        sbGreen.setMax((int) (colorSpace.getMaxValue(1) * 100.0f));
        sbBlue.setMin((int) (colorSpace.getMinValue(2) * 100.0f));
        sbBlue.setMax((int) (colorSpace.getMaxValue(2) * 100.0f));
        tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC);
        tietRed.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC_SIGNED);
        tietGreen.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC_SIGNED);
        tietBlue.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC_SIGNED);
        ((TextView) dialog.findViewById(R.id.tv_color_space)).setText(colorSpace.toString());
        sbAlpha.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) (seekBar, progress) -> tietAlpha.setText(String.valueOf(progress / 100.0f)));
        sbRed.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) (seekBar, progress) -> tietRed.setText(String.valueOf(progress / 100.0f)));
        sbGreen.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) (seekBar, progress) -> tietGreen.setText(String.valueOf(progress / 100.0f)));
        sbBlue.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) (seekBar, progress) -> tietBlue.setText(String.valueOf(progress / 100.0f)));
        tietAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(0, s, sbAlpha));
        tietRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(1, s, sbRed));
        tietGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(2, s, sbGreen));
        tietBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(3, s, sbBlue));

        tietAlpha.setText(String.valueOf(Color.alpha(oldColor)));
        loadColor(oldColor);

        final ColorPicker.OnColorPickListener onColorPickListener = (oldColor, newColor) -> loadColor(newColor);
        showOtherColorPickers(dialog, onColorPickListener);
    }
}
