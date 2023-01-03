package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

public class CmykColorPicker extends ColorPicker {

    private EditText etCyan, etMagenta, etYellow, etKey;
    private int radix = 16;
    private SeekBar sbCyan, sbMagenta, sbYellow, sbKey;
    private String format;

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorInt final Integer oldColor) {
        final Settings settings = ((MainApplication) context.getApplicationContext()).getSettings();
        final CmykColorPicker picker = new CmykColorPicker();
        picker.format = settings.getArgbComponentsFormat();
        picker.radix = settings.getArgbComponentsRadix();
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_from_cmyk)
                .setView(R.layout.cmyk_color_picker);

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
        newColor = Color.argb(0xFF, r, g, b);
        vPreview.setBackgroundColor(newColor);
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        etCyan = dialog.findViewById(R.id.et_cyan);
        etMagenta = dialog.findViewById(R.id.et_magenta);
        etYellow = dialog.findViewById(R.id.et_yellow);
        etKey = dialog.findViewById(R.id.et_key);
        sbCyan = dialog.findViewById(R.id.sb_cyan);
        sbMagenta = dialog.findViewById(R.id.sb_magenta);
        sbYellow = dialog.findViewById(R.id.sb_yellow);
        sbKey = dialog.findViewById(R.id.sb_key);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        sbCyan.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etCyan.setText(String.format(format, progress)));
        sbMagenta.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etMagenta.setText(String.format(format, progress)));
        sbYellow.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etYellow.setText(String.format(format, progress)));
        sbKey.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etKey.setText(String.format(format, progress)));
        etCyan.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbCyan));
        etMagenta.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbMagenta));
        etYellow.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbYellow));
        etKey.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbKey));

        final int r = Color.red(oldColor), g = Color.green(oldColor), b = Color.blue(oldColor);
        final int c_ = 0xFF - r, m_ = 0xFF - g, y_ = 0xFF - b;
        final int k = Math.min(Math.min(c_, m_), y_), invK = 0xFF - k;
        final int c = k == 0xFF ? 0x00 : (c_ - k) * 0xFF / invK,
                m = k == 0xFF ? 0x00 : (m_ - k) * 0xFF / invK,
                y = k == 0xFF ? 0x00 : (y_ - k) * 0xFF / invK;
        etCyan.setText(String.format(format, c));
        etMagenta.setText(String.format(format, m));
        etYellow.setText(String.format(format, y));
        etKey.setText(String.format(format, k));
    }
}
