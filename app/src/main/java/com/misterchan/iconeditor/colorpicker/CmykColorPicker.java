package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.widget.GridLayout;

import androidx.annotation.ColorLong;
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

class CmykColorPicker extends ColorPicker {

    private final int radix;
    private Slider sCyan, sMagenta, sYellow, sKey;
    private final String format;
    private TextInputEditText tietCyan, tietMagenta, tietYellow, tietKey;

    private CmykColorPicker(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        format = Settings.INST.argbCompFormat();
        radix = Settings.INST.argbCompRadix();
        dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(R.string.convert_cmyk_to_rgb)
                .setView(R.layout.color_picker);

        this.oldColor = oldColor;
    }

    static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return new CmykColorPicker(context, onColorPickListener, oldColor);
    }

    private void onComponentChanged(String s, Slider slider) {
        try {
            slider.setValue(Integer.parseUnsignedInt(s, radix));
        } catch (NumberFormatException e) {
        }
        final int c = (int) sCyan.getValue(), m = (int) sMagenta.getValue(), y = (int) sYellow.getValue(), k = (int) sKey.getValue();
        final int invK = 0xFF - k;
        final int r = 0xFF - c * invK / 0xFF - k, g = 0xFF - m * invK / 0xFF - k, b = 0xFF - y * invK / 0xFF - k;
        final int color = Color.argb(0xFF, r, g, b);
        newColor = Color.pack(color);
        vPreview.setBackgroundColor(color);
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        final GridLayout gl = dialog.findViewById(R.id.gl);
        sCyan = dialog.findViewById(R.id.s_comp_0);
        sMagenta = dialog.findViewById(R.id.s_comp_1);
        sYellow = dialog.findViewById(R.id.s_comp_2);
        sKey = dialog.findViewById(R.id.s_comp_3);
        final TextInputLayout tilCyan = dialog.findViewById(R.id.til_comp_0);
        final TextInputLayout tilMagenta = dialog.findViewById(R.id.til_comp_1);
        final TextInputLayout tilYellow = dialog.findViewById(R.id.til_comp_2);
        tietCyan = (TextInputEditText) tilCyan.getEditText();
        tietMagenta = (TextInputEditText) tilMagenta.getEditText();
        tietYellow = (TextInputEditText) tilYellow.getEditText();
        tietKey = dialog.findViewById(R.id.tiet_comp_3);
        vPreview = dialog.findViewById(R.id.v_color);

        hideOtherColorPickers(dialog);
        hideAlphaComp(gl);
        showExtraComp(gl);

        if (radix <= 10) {
            tietCyan.setInputType(EDITOR_TYPE_NUM);
            tietMagenta.setInputType(EDITOR_TYPE_NUM);
            tietYellow.setInputType(EDITOR_TYPE_NUM);
            tietKey.setInputType(EDITOR_TYPE_NUM);
        } else if (radix == 16) {
            tietCyan.setKeyListener(KEY_LISTENER_HEX);
            tietMagenta.setKeyListener(KEY_LISTENER_HEX);
            tietYellow.setKeyListener(KEY_LISTENER_HEX);
            tietKey.setKeyListener(KEY_LISTENER_HEX);
        }

        sCyan.setStepSize(1.0f);
        sMagenta.setStepSize(1.0f);
        sYellow.setStepSize(1.0f);
        sKey.setStepSize(1.0f);
        tilCyan.setHint(R.string.c);
        tilMagenta.setHint(R.string.m);
        tilYellow.setHint(R.string.y);
        sCyan.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietCyan.setText(String.format(format, (int) value)));
        sMagenta.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietMagenta.setText(String.format(format, (int) value)));
        sYellow.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietYellow.setText(String.format(format, (int) value)));
        sKey.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietKey.setText(String.format(format, (int) value)));
        tietCyan.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sCyan));
        tietMagenta.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sMagenta));
        tietYellow.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sYellow));
        tietKey.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sKey));

        final int color = Color.toArgb(oldColor);
        final int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        final int c_ = 0xFF - r, m_ = 0xFF - g, y_ = 0xFF - b;
        final int k = Math.min(Math.min(c_, m_), y_), invK = 0xFF - k;
        final int c = k == 0xFF ? 0x00 : (c_ - k) * 0xFF / invK,
                m = k == 0xFF ? 0x00 : (m_ - k) * 0xFF / invK,
                y = k == 0xFF ? 0x00 : (y_ - k) * 0xFF / invK;
        tietCyan.setText(String.format(format, c));
        tietMagenta.setText(String.format(format, m));
        tietYellow.setText(String.format(format, y));
        tietKey.setText(String.format(format, k));
    }
}
