package com.misterchan.iconeditor;

import android.content.Context;
import android.widget.GridLayout;
import android.widget.SeekBar;

import androidx.annotation.ColorLong;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CmykColorPicker extends ColorPicker {

    private int radix = 16;
    private SeekBar sbCyan, sbMagenta, sbYellow, sbKey;
    private String format;
    private TextInputEditText tietCyan, tietMagenta, tietYellow, tietKey;

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final Settings settings = ((MainApplication) context.getApplicationContext()).getSettings();
        final CmykColorPicker picker = new CmykColorPicker();
        picker.format = settings.getArgbComponentFormat();
        picker.radix = settings.getArgbComponentRadix();
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_cmyk_to_rgb)
                .setView(R.layout.color_picker);

        picker.oldColor = oldColor;

        return picker;
    }

    private void onComponentChanged(String s, SeekBar seekBar) {
        try {
            seekBar.setProgress(Integer.parseUnsignedInt(s, radix));
        } catch (NumberFormatException e) {
        }
        final int c = sbCyan.getProgress(), m = sbMagenta.getProgress(), y = sbYellow.getProgress(), k = sbKey.getProgress();
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
        sbCyan = dialog.findViewById(R.id.sb_comp_0);
        sbMagenta = dialog.findViewById(R.id.sb_comp_1);
        sbYellow = dialog.findViewById(R.id.sb_comp_2);
        sbKey = dialog.findViewById(R.id.sb_comp_3);
        tietCyan = dialog.findViewById(R.id.tiet_comp_0);
        tietMagenta = dialog.findViewById(R.id.tiet_comp_1);
        tietYellow = dialog.findViewById(R.id.tiet_comp_2);
        tietKey = dialog.findViewById(R.id.tiet_comp_3);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        hideOtherColorPickers(dialog);
        hideAlphaComp(gl);
        showExtraComp(gl);

        ((TextInputLayout) dialog.findViewById(R.id.til_comp_0)).setHint(R.string.c);
        ((TextInputLayout) dialog.findViewById(R.id.til_comp_1)).setHint(R.string.m);
        ((TextInputLayout) dialog.findViewById(R.id.til_comp_2)).setHint(R.string.y);
        sbCyan.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietCyan.setText(String.format(format, progress)));
        sbMagenta.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietMagenta.setText(String.format(format, progress)));
        sbYellow.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietYellow.setText(String.format(format, progress)));
        sbKey.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietKey.setText(String.format(format, progress)));
        tietCyan.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbCyan));
        tietMagenta.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbMagenta));
        tietYellow.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbYellow));
        tietKey.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbKey));

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
