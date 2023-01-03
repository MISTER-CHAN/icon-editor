package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.ColorSpace;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

public class LabColorPicker extends ColorPicker {

    private static final ColorSpace LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
    private static final ColorSpace.Connector CONNECTOR_RGB_LAB =
            ColorSpace.connect(ColorSpace.get(ColorSpace.Named.SRGB), LAB);

    private EditText etL, etA, etB;
    private SeekBar sbL, sbA, sbB;

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorInt final Integer oldColor) {
        final LabColorPicker picker = new LabColorPicker();
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_from_lab)
                .setView(R.layout.lab_color_picker);

        picker.oldColor = oldColor;

        return picker;
    }

    private void onComponentChanged(String s, SeekBar seekBar) {
        try {
            seekBar.setProgress((int) Float.parseFloat(s));
        } catch (NumberFormatException e) {
        }
        newColor = Color.toArgb(Color.pack(
                sbL.getProgress(), sbA.getProgress(), sbB.getProgress(), 1.0f, LAB));
        vPreview.setBackgroundColor(Color.BLACK | newColor);
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

        final float[] lab = CONNECTOR_RGB_LAB.transform(
                Color.red(oldColor) / 255.0f, Color.green(oldColor) / 255.0f, Color.blue(oldColor) / 255.0f);
        etL.setText(String.valueOf(lab[0]));
        etA.setText(String.valueOf(lab[1]));
        etB.setText(String.valueOf(lab[2]));
    }
}
