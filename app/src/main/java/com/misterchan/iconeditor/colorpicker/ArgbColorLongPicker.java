package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.graphics.ColorSpace;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;

public class ArgbColorLongPicker extends ArgbColorPicker {

    private ColorSpace colorSpace;

    @Size(4)
    private final float[] argb = new float[4];

    protected ArgbColorLongPicker(Context context, int titleId,
                                  final ColorPicker.OnColorPickListener onColorPickListener,
                                  @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        super(context, titleId, onColorPickListener, oldColor, neutralFunction);
    }

    private void loadColor(@ColorLong long color) {
        color = Color.convert(color, colorSpace);
        tietRed.setText(String.valueOf(argb[1] = Color.red(color)));
        tietGreen.setText(String.valueOf(argb[2] = Color.green(color)));
        tietBlue.setText(String.valueOf(argb[3] = Color.blue(color)));
    }

    protected void onComponentChanged(@IntRange(from = 0, to = 3) int index, String s, Slider slider) {
        try {
            final float f = Float.parseFloat(s);
            slider.setValue(f);
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
        sAlpha.setValueFrom(colorSpace.getMinValue(3));
        sAlpha.setValueTo(colorSpace.getMaxValue(3));
        sRed.setValueFrom(colorSpace.getMinValue(0));
        sRed.setValueTo(colorSpace.getMaxValue(0));
        sGreen.setValueFrom(colorSpace.getMinValue(1));
        sGreen.setValueTo(colorSpace.getMaxValue(1));
        sBlue.setValueFrom(colorSpace.getMinValue(2));
        sBlue.setValueTo(colorSpace.getMaxValue(2));
        tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC);
        tietRed.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC_SIGNED);
        tietGreen.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC_SIGNED);
        tietBlue.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC_SIGNED);
        ((TextView) dialog.findViewById(R.id.tv_color_space)).setText(colorSpace.toString());
        sAlpha.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietAlpha.setText(String.valueOf(value)));
        sRed.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietRed.setText(String.valueOf(value)));
        sGreen.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietGreen.setText(String.valueOf(value)));
        sBlue.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietBlue.setText(String.valueOf(value)));
        tietAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(0, s, sAlpha));
        tietRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(1, s, sRed));
        tietGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(2, s, sGreen));
        tietBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(3, s, sBlue));

        tietAlpha.setText(String.valueOf(Color.alpha(oldColor)));
        loadColor(oldColor);

        final ColorPicker.OnColorPickListener onColorPickListener = (oldColor, newColor) -> loadColor(newColor);
        showOtherColorPickers(dialog, onColorPickListener);
    }
}
