package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.widget.GridLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;

class HsvColorPicker extends ColorPicker {

    private final boolean hasAlpha;
    private final boolean type;
    private final int alphaRadix;
    private Slider sAlpha;
    private Slider sHue, sSaturation, sValue;
    private final String alphaFormat;
    private TextInputEditText tietAlpha;
    private TextInputEditText tietHue, tietSaturation, tietValue;

    @Size(3)
    private final float[] hsv = new float[3];

    private HsvColorPicker(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        this(context, R.string.convert_hsv_to_rgb, onColorPickListener, oldColor, 0);
    }

    HsvColorPicker(Context context, @StringRes int titleId,
                   final OnColorPickListener onColorPickListener,
                   @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(titleId)
                .setView(R.layout.color_picker);

        if (oldColor != null) {
            this.oldColor = oldColor;
            if (neutralFunction != 0) {
                dialogBuilder.setNeutralButton(neutralFunction,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, null));
            }
        } else {
            this.oldColor = Color.BLACK;
        }

        hasAlpha = Settings.INST.pickInHsv();
        type = Settings.INST.argbColorType();
        alphaFormat = type ? null : Settings.INST.argbCompFormat();
        alphaRadix = type ? 10 : Settings.INST.argbCompRadix();
    }

    static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return new HsvColorPicker(context, onColorPickListener, oldColor);
    }

    private void onAlphaChanged(String s) {
        try {
            sAlpha.setValue(type ? Float.parseFloat(s) : Integer.parseUnsignedInt(s, alphaRadix));
        } catch (NumberFormatException e) {
        }
        onComponentChanged();
    }

    private void onComponentChanged() {
        @ColorInt final int rgb = Color.HSVToColor(hsv);
        final int color = hasAlpha
                ? Color.argb((int) (type ? sAlpha.getValue() * 0xFF : sAlpha.getValue()), rgb)
                : Color.BLACK | rgb;
        newColor = Color.pack(color);
        vPreview.setBackgroundColor(color);
    }

    private void onHueChanged(String s) {
        try {
            float f = Float.parseFloat(s);
            sHue.setValue(f);
            hsv[0] = f % 360.0f;
        } catch (NumberFormatException e) {
        }
        onComponentChanged();
    }

    private void onSatOrValChanged(@IntRange(from = 1, to = 2) int index, String s, Slider slider) {
        try {
            float f = Float.parseFloat(s);
            slider.setValue(f);
            hsv[index] = f / 100.0f;
        } catch (NumberFormatException e) {
        }
        onComponentChanged();
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        final GridLayout gl = dialog.findViewById(R.id.gl);
        sHue = dialog.findViewById(R.id.s_comp_0);
        sSaturation = dialog.findViewById(R.id.s_comp_1);
        sValue = dialog.findViewById(R.id.s_comp_2);
        final TextInputLayout tilHue = dialog.findViewById(R.id.til_comp_0);
        final TextInputLayout tilSaturation = dialog.findViewById(R.id.til_comp_1);
        final TextInputLayout tilValue = dialog.findViewById(R.id.til_comp_2);
        tietHue = (TextInputEditText) tilHue.getEditText();
        tietSaturation = (TextInputEditText) tilSaturation.getEditText();
        tietValue = (TextInputEditText) tilValue.getEditText();
        vPreview = dialog.findViewById(R.id.v_color);
        if (hasAlpha) {
            sAlpha = dialog.findViewById(R.id.s_alpha);
            tietAlpha = dialog.findViewById(R.id.tiet_alpha);
        }

        hideOtherColorPickers(dialog);
        if (!hasAlpha) {
            hideAlphaComp(gl);
        }

        sHue.setValueTo(360.0f);
        sSaturation.setValueTo(100.0f);
        sValue.setValueTo(100.0f);
        tietHue.setInputType(EDITOR_TYPE_NUM_DEC);
        tietSaturation.setInputType(EDITOR_TYPE_NUM_DEC);
        tietValue.setInputType(EDITOR_TYPE_NUM_DEC);
        tilHue.setHint(R.string.h);
        tilHue.setSuffixText("°");
        tilSaturation.setHint(R.string.s);
        tilSaturation.setSuffixText("%");
        tilValue.setHint(R.string.v);
        tilValue.setSuffixText("%");
        sHue.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietHue.setText(String.valueOf(value)));
        sSaturation.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietSaturation.setText(String.valueOf(value)));
        sValue.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietValue.setText(String.valueOf(value)));
        tietHue.addTextChangedListener((AfterTextChangedListener) this::onHueChanged);
        tietSaturation.addTextChangedListener((AfterTextChangedListener) s -> onSatOrValChanged(1, s, sSaturation));
        tietValue.addTextChangedListener((AfterTextChangedListener) s -> onSatOrValChanged(2, s, sValue));
        if (hasAlpha) {
            sAlpha.setValueTo(type ? 1.0f : 0xFF);
            if (type) {
                tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC);
            } else {
                if (alphaRadix <= 10) {
                    tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM);
                } else if (alphaRadix == 16) {
                    tietAlpha.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
                }
            }
            sAlpha.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietAlpha.setText(type ? String.valueOf(value) : String.format(alphaFormat, (int) value)));
            tietAlpha.addTextChangedListener((AfterTextChangedListener) this::onAlphaChanged);
        }

        @ColorInt final int oldColorInt = Color.toArgb(oldColor);
        Color.colorToHSV(oldColorInt, hsv);
        tietHue.setText(String.valueOf(hsv[0]));
        tietSaturation.setText(String.valueOf(hsv[1] * 100.0f));
        tietValue.setText(String.valueOf(hsv[2] * 100.0f));
        if (hasAlpha) {
            tietAlpha.setText(type ? String.valueOf(Color.alpha(oldColor)) : String.format(alphaFormat, Color.alpha(oldColorInt)));
        }
    }
}
