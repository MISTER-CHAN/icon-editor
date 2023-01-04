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

public class XyYColorPicker extends ColorPicker {

    private static final ColorSpace XYZ = ColorSpace.get(ColorSpace.Named.CIE_XYZ);

    private ColorSpace oldColorSpace;
    private ColorSpace.Connector connectorFromXyz, connectorToXyz;
    private EditText etX_, etY_, etY;
    private SeekBar sbX_, sbY_, sbY;

    @Size(3)
    private final float[] xyY = new float[3];

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final XyYColorPicker picker = new XyYColorPicker();
        picker.oldColorSpace = Color.colorSpace(oldColor);
        picker.connectorFromXyz = ColorSpace.connect(XYZ, picker.oldColorSpace);
        picker.connectorToXyz = ColorSpace.connect(picker.oldColorSpace, XYZ);
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(R.string.convert_xyy_to_rgb)
                .setView(R.layout.xyy_color_picker);

        picker.oldColor = oldColor;

        return picker;
    }

    private void onComponentChanged() {
        final float yoy = xyY[2] / xyY[1], x = yoy * xyY[0], z = yoy * (1.0f - xyY[0] - xyY[1]);
        newColor = Color.pack(x, xyY[2], z, 1.0f, XYZ);
        vPreview.setBackgroundColor(toArgb(x, xyY[2], z));
    }

    private void onXyChanged(@IntRange(from = 0, to = 1) int index, String s, SeekBar seekBar) {
        try {
            float f = Float.parseFloat(s);
            seekBar.setProgress((int) (f * 250.0f));
            xyY[index] = f;
        } catch (NumberFormatException e) {
        }
        onComponentChanged();
    }

    private void onYChanged(String s) {
        try {
            float f = Float.parseFloat(s);
            sbY.setProgress((int) (f * 100.0f));
            xyY[2] = f;
        } catch (NumberFormatException e) {
        }
        onComponentChanged();
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        etX_ = dialog.findViewById(R.id.et_x_);
        etY_ = dialog.findViewById(R.id.et_y_);
        etY = dialog.findViewById(R.id.et_y);
        sbX_ = dialog.findViewById(R.id.sb_x_);
        sbY_ = dialog.findViewById(R.id.sb_y_);
        sbY = dialog.findViewById(R.id.sb_y);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        sbX_.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etX_.setText(String.valueOf((float) progress / 250.0f)));
        sbY_.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etY_.setText(String.valueOf((float) progress / 250.0f)));
        sbY.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etY.setText(String.valueOf((float) progress / 100.0f)));
        etX_.addTextChangedListener((AfterTextChangedListener) s -> onXyChanged(0, s, sbX_));
        etY_.addTextChangedListener((AfterTextChangedListener) s -> onXyChanged(1, s, sbY_));
        etY.addTextChangedListener((AfterTextChangedListener) this::onYChanged);

        final float[] xyz = connectorToXyz.transform(
                Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor));
        final float sum = xyz[0] + xyz[1] + xyz[2];
        if (sum == 0.0f) {
            xyY[0] = xyY[1] = xyY[2] = 0.0f;
        } else {
            xyY[0] = xyz[0] / sum;
            xyY[1] = xyz[1] / sum;
            xyY[2] = xyz[1];
        }
        etX_.setText(String.valueOf(xyY[0]));
        etY_.setText(String.valueOf(xyY[1]));
        etY.setText(String.valueOf(xyY[2]));
    }

    @ColorInt
    public int toArgb(float x, float y, float z) {
        float[] c = connectorFromXyz.transform(x, y, z);
        return Color.BLACK |
                ((int) (c[0] * 255.0f + 0.5f) << 16) |
                ((int) (c[1] * 255.0f + 0.5f) << 8) |
                (int) (c[2] * 255.0f + 0.5f);
    }
}
