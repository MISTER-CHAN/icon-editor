package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.ColorSpace;
import android.view.inputmethod.EditorInfo;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

class LabColorPicker extends ColorPicker {

    private static final ColorSpace LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);

    private ColorSpace oldColorSpace;
    private ColorSpace.Connector connectorFromLab, connectorToLab;
    private SeekBar sbL, sbA, sbB;
    private TextInputEditText tietL, tietA, tietB;

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final LabColorPicker picker = new LabColorPicker();
        picker.oldColorSpace = Color.colorSpace(oldColor);
        picker.connectorFromLab = ColorSpace.connect(LAB, picker.oldColorSpace);
        picker.connectorToLab = ColorSpace.connect(picker.oldColorSpace, LAB);
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_lab_to_rgb)
                .setView(R.layout.color_picker);

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

        sbL = dialog.findViewById(R.id.sb_comp_0);
        sbA = dialog.findViewById(R.id.sb_comp_1);
        sbB = dialog.findViewById(R.id.sb_comp_2);
        final TextInputLayout tilL = dialog.findViewById(R.id.til_comp_0);
        final TextInputLayout tilA = dialog.findViewById(R.id.til_comp_1);
        final TextInputLayout tilB = dialog.findViewById(R.id.til_comp_2);
        tietL = (TextInputEditText) tilL.getEditText();
        tietA = (TextInputEditText) tilA.getEditText();
        tietB = (TextInputEditText) tilB.getEditText();
        vPreview = dialog.findViewById(R.id.v_color_preview);

        hideOtherColorPickers(dialog);
        hideAlphaComp(dialog.findViewById(R.id.gl));

        sbL.setMax(100);
        sbA.setMin(-128);
        sbA.setMax(128);
        sbB.setMin(-128);
        sbB.setMax(128);
        tietL.setInputType(EDITOR_TYPE_NUM_DEC);
        tietA.setInputType(EDITOR_TYPE_NUM_DEC_SIGNED);
        tietB.setInputType(EDITOR_TYPE_NUM_DEC_SIGNED);
        tilL.setHint(R.string.l_);
        tilA.setHint(R.string.a_);
        tilB.setHint(R.string.b_);
        sbL.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietL.setText(String.valueOf(progress)));
        sbA.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietA.setText(String.valueOf(progress)));
        sbB.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietB.setText(String.valueOf(progress)));
        tietL.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbL));
        tietA.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbA));
        tietB.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbB));

        final float[] lab = connectorToLab.transform(
                Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor));
        tietL.setText(String.valueOf(lab[0]));
        tietA.setText(String.valueOf(lab[1]));
        tietB.setText(String.valueOf(lab[2]));
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
