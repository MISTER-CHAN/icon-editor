package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.ColorSpace;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

class XyYColorPicker extends ColorPicker {

    private static final ColorSpace XYZ = ColorSpace.get(ColorSpace.Named.CIE_XYZ);

    private final ColorSpace.Connector connectorFromXyz, connectorToXyz;
    private SeekBar sbX_, sbY_, sbY;
    private TextInputEditText tietX_, tietY_, tietY;

    @Size(3)
    private final float[] xyY = new float[3];

    private XyYColorPicker(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final ColorSpace oldColorSpace = Color.colorSpace(oldColor);
        connectorFromXyz = ColorSpace.connect(XYZ, oldColorSpace);
        connectorToXyz = ColorSpace.connect(oldColorSpace, XYZ);
        dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(R.string.convert_xyy_to_rgb)
                .setView(R.layout.color_picker);

        this.oldColor = oldColor;
    }

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return new XyYColorPicker(context, onColorPickListener, oldColor);
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

        sbX_ = dialog.findViewById(R.id.sb_comp_0);
        sbY_ = dialog.findViewById(R.id.sb_comp_1);
        sbY = dialog.findViewById(R.id.sb_comp_2);
        final TextInputLayout tilX_ = dialog.findViewById(R.id.til_comp_0);
        final TextInputLayout tilY_ = dialog.findViewById(R.id.til_comp_1);
        final TextInputLayout tilY = dialog.findViewById(R.id.til_comp_2);
        tietX_ = (TextInputEditText) tilX_.getEditText();
        tietY_ = (TextInputEditText) tilY_.getEditText();
        tietY = (TextInputEditText) tilY.getEditText();
        vPreview = dialog.findViewById(R.id.v_color);

        hideOtherColorPickers(dialog);
        hideAlphaComp(dialog.findViewById(R.id.gl));

        sbX_.setMax(200);
        sbY_.setMax(225);
        sbY.setMin(-200);
        sbY.setMax(200);
        tietX_.setInputType(EDITOR_TYPE_NUM_DEC);
        tietY_.setInputType(EDITOR_TYPE_NUM_DEC);
        tietY.setInputType(EDITOR_TYPE_NUM_DEC_SIGNED);
        tilX_.setHint(R.string.x_);
        tilY_.setHint(R.string.y_);
        tilY.setHint(R.string.y);
        sbX_.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietX_.setText(String.valueOf((float) progress / 250.0f)));
        sbY_.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietY_.setText(String.valueOf((float) progress / 250.0f)));
        sbY.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> tietY.setText(String.valueOf((float) progress / 100.0f)));
        tietX_.addTextChangedListener((AfterTextChangedListener) s -> onXyChanged(0, s, sbX_));
        tietY_.addTextChangedListener((AfterTextChangedListener) s -> onXyChanged(1, s, sbY_));
        tietY.addTextChangedListener((AfterTextChangedListener) this::onYChanged);

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
        tietX_.setText(String.valueOf(xyY[0]));
        tietY_.setText(String.valueOf(xyY[1]));
        tietY.setText(String.valueOf(xyY[2]));
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
