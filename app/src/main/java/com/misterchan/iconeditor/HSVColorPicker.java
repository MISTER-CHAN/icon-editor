package com.misterchan.iconeditor;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

public class HSVColorPicker extends ColorPicker {

    private EditText etHue;
    private EditText etSaturation;
    private EditText etValue;
    private SeekBar sbHue;
    private SeekBar sbSaturation;
    private SeekBar sbValue;

    @Size(3)
    private final float[] hsv = new float[3];

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorInt final Integer oldColor) {
        final HSVColorPicker picker = new HSVColorPicker();
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_from_hsv)
                .setView(R.layout.hsv_color_picker);

        picker.oldColor = oldColor;

        return picker;
    }

    private void onComponentChanged() {
        newColor = Color.HSVToColor(hsv);
        vPreview.setBackgroundColor(Color.BLACK | newColor);
    }

    private void onHueChanged(String s) {
        try {
            float f = Float.parseFloat(s);
            sbHue.setProgress((int) f);
            hsv[0] = f % 360.0f;
        } catch (NumberFormatException e) {
        }
        onComponentChanged();
    }

    private void onSatOrValChanged(@IntRange(from = 1, to = 2) int index, String s, SeekBar seekBar) {
        try {
            float f = Float.parseFloat(s);
            seekBar.setProgress((int) f);
            hsv[index] = f / 100.0f;
        } catch (NumberFormatException e) {
        }
        onComponentChanged();
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        etHue = dialog.findViewById(R.id.et_hue);
        etSaturation = dialog.findViewById(R.id.et_saturation);
        etValue = dialog.findViewById(R.id.et_value);
        sbHue = dialog.findViewById(R.id.sb_hue);
        sbSaturation = dialog.findViewById(R.id.sb_saturation);
        sbValue = dialog.findViewById(R.id.sb_value);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        sbHue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etHue.setText(String.valueOf(progress)));
        sbSaturation.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etSaturation.setText(String.valueOf(progress)));
        sbValue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etValue.setText(String.valueOf(progress)));
        etHue.addTextChangedListener((AfterTextChangedListener) this::onHueChanged);
        etSaturation.addTextChangedListener((AfterTextChangedListener) s -> onSatOrValChanged(1, s, sbSaturation));
        etValue.addTextChangedListener((AfterTextChangedListener) s -> onSatOrValChanged(2, s, sbValue));

        Color.colorToHSV(oldColor, hsv);
        etHue.setText(String.valueOf(hsv[0]));
        etSaturation.setText(String.valueOf(hsv[1] * 100.0f));
        etValue.setText(String.valueOf(hsv[2] * 100.0f));
    }
}
