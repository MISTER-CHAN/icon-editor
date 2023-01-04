package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.ColorSpace;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.appcompat.app.AlertDialog;

public class LabColorPicker extends ColorPicker {

    private static final ColorSpace LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);

    private ColorSpace oldColorSpace;
    private ColorSpace.Connector connectorFromLab, connectorToLab;
    private EditText etL, etA, etB;
    private SeekBar sbL, sbA, sbB;

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final LabColorPicker picker = new LabColorPicker();
        picker.oldColorSpace = Color.colorSpace(oldColor);
        picker.connectorFromLab = ColorSpace.connect(LAB, picker.oldColorSpace);
        picker.connectorToLab = ColorSpace.connect(picker.oldColorSpace, LAB);
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_lab_to_rgb)
                .setView(R.layout.lab_color_picker);

        picker.oldColor = oldColor;

        return picker;
    }

    private void onComponentChanged(String s, SeekBar seekBar) {
        try {
            seekBar.setProgress((int) Float.parseFloat(s));
        } catch (NumberFormatException e) {
        }
        final float l = sbL.getProgress(), a = sbA.getProgress(), b = sbB.getProgress();
        newColor = Color.pack(l, a, b, 1.0f, LAB);
        vPreview.setBackgroundColor(toArgb(l, a, b));
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        etL = dialog.findViewById(R.id.et_l);
        etA = dialog.findViewById(R.id.et_a);
        etB = dialog.findViewById(R.id.et_b);
        sbL = dialog.findViewById(R.id.sb_l);
        sbA = dialog.findViewById(R.id.sb_a);
        sbB = dialog.findViewById(R.id.sb_b);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        sbL.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etL.setText(String.valueOf(progress)));
        sbA.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etA.setText(String.valueOf(progress)));
        sbB.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etB.setText(String.valueOf(progress)));
        etL.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbL));
        etA.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbA));
        etB.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbB));

        final float[] lab = connectorToLab.transform(
                Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor));
        etL.setText(String.valueOf(lab[0]));
        etA.setText(String.valueOf(lab[1]));
        etB.setText(String.valueOf(lab[2]));
    }

    @ColorInt
    public int toArgb(float l, float a, float b) {
        float[] c = connectorFromLab.transform(l, a, b);
        return Color.BLACK |
                ((int) (c[0] * 255.0f + 0.5f) << 16) |
                ((int) (c[1] * 255.0f + 0.5f) << 8) |
                (int) (c[2] * 255.0f + 0.5f);
    }
}
