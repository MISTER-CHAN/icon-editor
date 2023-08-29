package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.graphics.ColorSpace;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;

public class ArgbColorPicker extends ColorPicker {

    private final boolean type;
    private ColorSpace colorSpace;
    private final Context context;
    private final int radix;
    private Slider sAlpha;
    private Slider sRed, sGreen, sBlue;
    private final String format;
    private TextInputEditText tietAlpha;
    private TextInputEditText tietRed, tietGreen, tietBlue;

    private ArgbColorPicker(Context context, @StringRes int titleId,
                            final OnColorPickListener onColorPickListener,
                            @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        this.context = context;
        dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(titleId)
                .setView(R.layout.color_picker_argb);

        if (oldColor != null) {
            this.oldColor = oldColor;
            if (neutralFunction != 0) {
                dialogBuilder.setNeutralButton(neutralFunction,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, null));
            }
        } else {
            this.oldColor = Color.BLACK;
        }

        type = Settings.INST.argbColorType();
        format = type ? null : Settings.INST.argbCompFormat();
        radix = type ? 10 : Settings.INST.argbCompRadix();
    }

    private void loadColor(@ColorInt int color, boolean hasAlpha) {
        if (hasAlpha) {
            tietAlpha.setText(String.format(format, Color.alpha(color)));
        }
        tietRed.setText(String.format(format, Color.red(color)));
        tietGreen.setText(String.format(format, Color.green(color)));
        tietBlue.setText(String.format(format, Color.blue(color)));
    }

    private void loadColor(@ColorLong long color, boolean hasAlpha) {
        color = Color.convert(color, colorSpace);
        if (hasAlpha) {
            tietAlpha.setText(String.valueOf(Color.alpha(color)));
        }
        tietRed.setText(String.valueOf(Color.red(color)));
        tietGreen.setText(String.valueOf(Color.green(color)));
        tietBlue.setText(String.valueOf(Color.blue(color)));
    }

    public static ColorPicker make(Context context, int titleId,
                                   final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return make(context, titleId, onColorPickListener, oldColor, 0);
    }

    public static ColorPicker make(Context context, @StringRes int titleId,
                                   final OnColorPickListener onColorPickListener,
                                   @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        return Settings.INST.pickInHsv()
                ? new HsvColorPicker(context, titleId, onColorPickListener, oldColor, neutralFunction)
                : new ArgbColorPicker(context, titleId, onColorPickListener, oldColor, neutralFunction);
    }

    private void onComponentChanged(String s, Slider slider) {
        try {
            slider.setValue(type ? Float.parseFloat(s) : Integer.parseUnsignedInt(s, radix));
        } catch (NumberFormatException e) {
        }
        final float av = sAlpha.getValue(), rv = sRed.getValue(), gv = sGreen.getValue(), bv = sBlue.getValue();
        @ColorInt final int newColorInt = type
                ? Color.argb(av, rv, gv, bv)
                : Color.argb((int) av, (int) rv, (int) gv, (int) bv);
        newColor = type
                ? Color.pack(rv, gv, bv, av)
                : Color.pack(newColorInt);
        vPreview.setBackgroundColor(newColorInt);
    }

    private void showOtherColorPickers(AlertDialog dialog, OnColorPickListener l) {
        dialog.findViewById(R.id.b_cmyk).setOnClickListener(v ->
                CmykColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.b_hsv).setOnClickListener(v ->
                HsvColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.b_lab).setOnClickListener(v ->
                LabColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.b_xyz).setOnClickListener(v ->
                XyzColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.b_xyy).setOnClickListener(v ->
                XyYColorPicker.make(context, l, newColor).show());
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        sAlpha = dialog.findViewById(R.id.s_alpha);
        sRed = dialog.findViewById(R.id.s_comp_0);
        sGreen = dialog.findViewById(R.id.s_comp_1);
        sBlue = dialog.findViewById(R.id.s_comp_2);
        tietAlpha = dialog.findViewById(R.id.tiet_alpha);
        tietRed = dialog.findViewById(R.id.tiet_comp_0);
        tietGreen = dialog.findViewById(R.id.tiet_comp_1);
        tietBlue = dialog.findViewById(R.id.tiet_comp_2);
        vPreview = dialog.findViewById(R.id.v_color);

        if (type) {
            colorSpace = Color.colorSpace(oldColor);

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
        } else {
            sAlpha.setStepSize(1.0f);
            sRed.setStepSize(1.0f);
            sGreen.setStepSize(1.0f);
            sBlue.setStepSize(1.0f);
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
        }

        sAlpha.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietAlpha.setText(type ? String.valueOf(value) : String.format(format, (int) value)));
        sRed.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietRed.setText(type ? String.valueOf(value) : String.format(format, (int) value)));
        sGreen.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietGreen.setText(type ? String.valueOf(value) : String.format(format, (int) value)));
        sBlue.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietBlue.setText(type ? String.valueOf(value) : String.format(format, (int) value)));
        tietAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sAlpha));
        tietRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sRed));
        tietGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sGreen));
        tietBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sBlue));

        if (type) {
            loadColor(oldColor, true);
        } else {
            loadColor(Color.toArgb(oldColor), true);
        }

        showOtherColorPickers(dialog, (oldColor, newColor) -> {
            if (type) {
                loadColor(newColor, false);
            } else {
                loadColor(Color.toArgb(newColor), false);
            }
        });
    }
}
