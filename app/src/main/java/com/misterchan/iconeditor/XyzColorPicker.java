package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.ColorSpace;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

public class XyzColorPicker extends ColorPicker {

    private static final ColorSpace XYZ = ColorSpace.get(ColorSpace.Named.CIE_XYZ);

    private ColorSpace oldColorSpace;
    private ColorSpace.Connector connectorFromXyz, connectorToXyz;
    private EditText etX, etY, etZ;
    private SeekBar sbX, sbY, sbZ;

    @Size(3)
    private float[] xyz;

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final XyzColorPicker picker = new XyzColorPicker();
        picker.oldColorSpace = Color.colorSpace(oldColor);
        picker.connectorFromXyz = ColorSpace.connect(XYZ, picker.oldColorSpace);
        picker.connectorToXyz = ColorSpace.connect(picker.oldColorSpace, XYZ);
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_xyz_to_rgb)
                .setView(R.layout.xyz_color_picker);

        picker.oldColor = oldColor;

        return picker;
    }

    private void onComponentChanged(@IntRange(from = 0, to = 2) int index, String s, SeekBar seekBar) {
        try {
            float f = Float.parseFloat(s);
            seekBar.setProgress((int) (f * 100.0f));
            xyz[index] = f;
        } catch (NumberFormatException e) {
        }
        newColor = Color.pack(xyz[0], xyz[1], xyz[2], 1.0f, XYZ);
        vPreview.setBackgroundColor(toArgb());
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        etX = dialog.findViewById(R.id.et_x);
        etY = dialog.findViewById(R.id.et_y);
        etZ = dialog.findViewById(R.id.et_z);
        sbX = dialog.findViewById(R.id.sb_x);
        sbY = dialog.findViewById(R.id.sb_y);
        sbZ = dialog.findViewById(R.id.sb_z);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        sbX.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etX.setText(String.valueOf((float) progress / 100.0f)));
        sbY.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etY.setText(String.valueOf((float) progress / 100.0f)));
        sbZ.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etZ.setText(String.valueOf((float) progress / 100.0f)));
        etX.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(0, s, sbX));
        etY.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(1, s, sbY));
        etZ.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(2, s, sbZ));

        xyz = connectorToXyz.transform(
                Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor));
        etX.setText(String.valueOf(xyz[0]));
        etY.setText(String.valueOf(xyz[1]));
        etZ.setText(String.valueOf(xyz[2]));
    }

    @ColorInt
    public int toArgb() {
        float[] c = connectorFromXyz.transform(xyz[0], xyz[1], xyz[2]);
        return Color.BLACK |
                ((int) (c[0] * 255.0f + 0.5f) << 16) |
                ((int) (c[1] * 255.0f + 0.5f) << 8) |
                (int) (c[2] * 255.0f + 0.5f);
    }
}
