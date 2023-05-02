package com.misterchan.iconeditor.colorpicker;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;

public class ArgbColorIntPicker extends ArgbColorPicker {

    private int radix = 16;
    private String format;

    protected ArgbColorIntPicker(Context context, int titleId,
                                 final ColorPicker.OnColorPickListener onColorPickListener,
                                 @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        super(context, titleId, onColorPickListener, oldColor, neutralFunction);
    }

    private void loadColor(@ColorInt int color) {
        tietRed.setText(String.format(format, Color.red(color)));
        tietGreen.setText(String.format(format, Color.green(color)));
        tietBlue.setText(String.format(format, Color.blue(color)));
    }

    @Override
    protected void make() {
        format = Settings.INST.argbCompFormat();
        radix = Settings.INST.argbCompRadix();
    }

    protected void onComponentChanged(String s, Slider slider) {
        try {
            slider.setValue(Integer.parseUnsignedInt(s, radix));
        } catch (NumberFormatException e) {
        }
        final int color = Color.argb((int) sAlpha.getValue(),
                (int) sRed.getValue(), (int) sGreen.getValue(), (int) sBlue.getValue());
        newColor = Color.pack(color);
        vPreview.setBackgroundColor(color);
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        initViews(dialog);

        if (radix <= 10) {
            tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM);
            tietRed.setInputType(ColorPicker.EDITOR_TYPE_NUM);
            tietGreen.setInputType(ColorPicker.EDITOR_TYPE_NUM);
            tietBlue.setInputType(ColorPicker.EDITOR_TYPE_NUM);
        } else if (radix == 16) {
            tietAlpha.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
            tietRed.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
            tietGreen.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
            tietBlue.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
        }

        sAlpha.setStepSize(1.0f);
        sRed.setStepSize(1.0f);
        sGreen.setStepSize(1.0f);
        sBlue.setStepSize(1.0f);
        sAlpha.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietAlpha.setText(String.format(format, (int) value)));
        sRed.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietRed.setText(String.format(format, (int) value)));
        sGreen.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietGreen.setText(String.format(format, (int) value)));
        sBlue.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietBlue.setText(String.format(format, (int) value)));
        tietAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sAlpha));
        tietRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sRed));
        tietGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sGreen));
        tietBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sBlue));

        final int color = Color.toArgb(oldColor);
        tietAlpha.setText(String.format(format, Color.alpha(color)));
        loadColor(color);

        final ColorPicker.OnColorPickListener onColorPickListener = (oldColor, newColor) -> loadColor(Color.toArgb(newColor));
        showOtherColorPickers(dialog, onColorPickListener);
    }
}
