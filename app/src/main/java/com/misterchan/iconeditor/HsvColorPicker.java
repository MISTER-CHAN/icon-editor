package com.misterchan.iconeditor;

import android.content.Context;
import android.widget.GridLayout;
import android.widget.SeekBar;

import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class HsvColorPicker extends ColorPicker {

    private SeekBar sbHue, sbSaturation, sbValue;
    private TextInputEditText tietHue, tietSaturation, tietValue;

    @Size(3)
    private final float[] hsv = new float[3];

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final HsvColorPicker picker = new HsvColorPicker();
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_hsv_to_rgb)
                .setView(R.layout.color_picker);

        picker.oldColor = oldColor;

        return picker;
    }

    private void onComponentChanged() {
        final int color = Color.HSVToColor(hsv);
        newColor = Color.pack(color);
        vPreview.setBackgroundColor(Color.BLACK | color);
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

        final GridLayout gl = dialog.findViewById(R.id.gl);
        sbHue = dialog.findViewById(R.id.sb_comp_0);
        sbSaturation = dialog.findViewById(R.id.sb_comp_1);
        sbValue = dialog.findViewById(R.id.sb_comp_2);
        tietHue = dialog.findViewById(R.id.tiet_comp_0);
        tietSaturation = dialog.findViewById(R.id.tiet_comp_1);
        tietValue = dialog.findViewById(R.id.tiet_comp_2);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        hideOtherColorPickers(dialog);
        hideAlphaComp(gl);
        showUnits(gl);

        sbHue.setMax(360);
        sbSaturation.setMax(100);
        sbValue.setMax(100);
        tietHue.setInputType(EDITOR_TYPE_NUM_DEC);
        tietSaturation.setInputType(EDITOR_TYPE_NUM_DEC);
        tietValue.setInputType(EDITOR_TYPE_NUM_DEC);
        ((TextInputLayout) dialog.findViewById(R.id.til_comp_0)).setHint(R.string.h);
        ((TextInputLayout) dialog.findViewById(R.id.til_comp_1)).setHint(R.string.s);
        ((TextInputLayout) dialog.findViewById(R.id.til_comp_2)).setHint(R.string.v);
        sbHue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietHue.setText(String.valueOf(progress)));
        sbSaturation.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietSaturation.setText(String.valueOf(progress)));
        sbValue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietValue.setText(String.valueOf(progress)));
        tietHue.addTextChangedListener((AfterTextChangedListener) this::onHueChanged);
        tietSaturation.addTextChangedListener((AfterTextChangedListener) s -> onSatOrValChanged(1, s, sbSaturation));
        tietValue.addTextChangedListener((AfterTextChangedListener) s -> onSatOrValChanged(2, s, sbValue));

        Color.colorToHSV(Color.toArgb(oldColor), hsv);
        tietHue.setText(String.valueOf(hsv[0]));
        tietSaturation.setText(String.valueOf(hsv[1] * 100.0f));
        tietValue.setText(String.valueOf(hsv[2] * 100.0f));
    }
}
