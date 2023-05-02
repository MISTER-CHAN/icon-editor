package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.graphics.ColorSpace;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;

public class XyzColorPicker extends ColorPicker {

    private static final ColorSpace XYZ = ColorSpace.get(ColorSpace.Named.CIE_XYZ);

    private final ColorSpace.Connector connectorFromXyz, connectorToXyz;
    private Slider sX, sY, sZ;
    private TextInputEditText tietX, tietY, tietZ;

    @Size(3)
    private float[] xyz;

    private XyzColorPicker(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        final ColorSpace oldColorSpace = Color.colorSpace(oldColor);
        connectorFromXyz = ColorSpace.connect(XYZ, oldColorSpace);
        connectorToXyz = ColorSpace.connect(oldColorSpace, XYZ);
        dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(R.string.convert_xyz_to_rgb)
                .setView(R.layout.color_picker);

        this.oldColor = oldColor;
    }

    public static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return new XyzColorPicker(context, onColorPickListener, oldColor);
    }

    private void onComponentChanged(@IntRange(from = 0, to = 2) int index, String s, Slider slider) {
        try {
            float f = Float.parseFloat(s);
            slider.setValue(f);
            xyz[index] = f;
        } catch (NumberFormatException e) {
        }
        newColor = Color.pack(xyz[0], xyz[1], xyz[2], 1.0f, XYZ);
        vPreview.setBackgroundColor(toArgb());
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        sX = dialog.findViewById(R.id.s_comp_0);
        sY = dialog.findViewById(R.id.s_comp_1);
        sZ = dialog.findViewById(R.id.s_comp_2);
        final TextInputLayout tilX = dialog.findViewById(R.id.til_comp_0);
        final TextInputLayout tilY = dialog.findViewById(R.id.til_comp_1);
        final TextInputLayout tilZ = dialog.findViewById(R.id.til_comp_2);
        tietX = (TextInputEditText) tilX.getEditText();
        tietY = (TextInputEditText) tilY.getEditText();
        tietZ = (TextInputEditText) tilZ.getEditText();
        vPreview = dialog.findViewById(R.id.v_color);

        hideOtherColorPickers(dialog);
        hideAlphaComp(dialog.findViewById(R.id.gl));

        sX.setValueFrom(-2.0f);
        sX.setValueTo(2.0f);
        sY.setValueFrom(-2.0f);
        sY.setValueTo(2.0f);
        sZ.setValueFrom(-2.0f);
        sZ.setValueTo(2.0f);
        tietX.setInputType(EDITOR_TYPE_NUM_DEC_SIGNED);
        tietY.setInputType(EDITOR_TYPE_NUM_DEC_SIGNED);
        tietZ.setInputType(EDITOR_TYPE_NUM_DEC_SIGNED);
        tilX.setHint(R.string.x);
        tilY.setHint(R.string.y);
        tilZ.setHint(R.string.z);
        sX.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietX.setText(String.valueOf(value)));
        sY.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietY.setText(String.valueOf(value)));
        sZ.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietZ.setText(String.valueOf(value)));
        tietX.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(0, s, sX));
        tietY.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(1, s, sY));
        tietZ.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(2, s, sZ));

        xyz = connectorToXyz.transform(
                Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor));
        tietX.setText(String.valueOf(xyz[0]));
        tietY.setText(String.valueOf(xyz[1]));
        tietZ.setText(String.valueOf(xyz[2]));
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
