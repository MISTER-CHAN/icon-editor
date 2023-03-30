package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.graphics.ColorSpace;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSeekBarProgressChangedListener;

public class LabColorPicker extends ColorPicker {

    private static final ColorSpace LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);

    private final ColorSpace.Connector connectorFromLab, connectorToLab;
    private SeekBar sbL, sbA, sbB;
    private TextInputEditText tietL, tietA, tietB;

    private LabColorPicker(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final ColorSpace oldColorSpace = Color.colorSpace(oldColor);
        connectorFromLab = ColorSpace.connect(LAB, oldColorSpace);
        connectorToLab = ColorSpace.connect(oldColorSpace, LAB);
        dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(R.string.convert_lab_to_rgb)
                .setView(R.layout.color_picker);

        this.oldColor = oldColor;
    }

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return new LabColorPicker(context, onColorPickListener, oldColor);
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
        vPreview = dialog.findViewById(R.id.v_color);

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
        sbL.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) (seekBar, progress) -> tietL.setText(String.valueOf(progress)));
        sbA.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) (seekBar, progress) -> tietA.setText(String.valueOf(progress)));
        sbB.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) (seekBar, progress) -> tietB.setText(String.valueOf(progress)));
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
