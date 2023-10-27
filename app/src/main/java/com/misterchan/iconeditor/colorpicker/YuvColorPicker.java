package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.graphics.ColorMatrix;

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

class YuvColorPicker extends ColorPicker {
    private Slider sY, sU, sV;
    private TextInputEditText tietY, tietU, tietV;

    @Size(3)
    private final float[] yuv = new float[3];

    private static final float[] RGB_TO_YUV = new ColorMatrix() {
        {
            setRGB2YUV();
        }
    }.getArray();

    private static final float[] YUV_TO_RGB = new ColorMatrix() {
        {
            setYUV2RGB();
        }
    }.getArray();


    private YuvColorPicker(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(R.string.convert_yuv_to_rgb)
                .setView(R.layout.color_picker);

        this.oldColor = oldColor;
    }

    public static void convert(@Size(3) final float[] src, @Size(3) final float[] dst, @Size(20) final float[] matrix) {
        dst[0] = src[0] * matrix[0] + src[1] * matrix[1] + src[2] * matrix[2];
        dst[1] = src[0] * matrix[5] + src[1] * matrix[6] + src[2] * matrix[7];
        dst[2] = src[0] * matrix[10] + src[1] * matrix[11] + src[2] * matrix[12];
    }

    static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return new YuvColorPicker(context, onColorPickListener, oldColor);
    }

    private void onComponentChanged(@IntRange(from = 0, to = 2) int index, String s, Slider slider) {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        if (!(slider.getValueFrom() <= f && f <= slider.getValueTo())) return;
        slider.setValue(f);
        yuv[index] = f;
        @Size(3) final float[] rgb = new float[3];
        convert(yuv, rgb, YUV_TO_RGB);
        newColor = Color.pack(rgb[0], rgb[1], rgb[2]);
        vPreview.setBackgroundColor(Color.toArgb(newColor));
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        sY = dialog.findViewById(R.id.s_comp_0);
        sU = dialog.findViewById(R.id.s_comp_1);
        sV = dialog.findViewById(R.id.s_comp_2);
        final TextInputLayout tilY = dialog.findViewById(R.id.til_comp_0);
        final TextInputLayout tilU = dialog.findViewById(R.id.til_comp_1);
        final TextInputLayout tilV = dialog.findViewById(R.id.til_comp_2);
        tietY = (TextInputEditText) tilY.getEditText();
        tietU = (TextInputEditText) tilU.getEditText();
        tietV = (TextInputEditText) tilV.getEditText();
        vPreview = dialog.findViewById(R.id.v_color);

        hideAlphaComp(dialog.findViewById(R.id.gl));

        sY.setValueTo(1.0f);
        sU.setValueFrom(-0.5f);
        sU.setValueTo(0.5f);
        sV.setValueFrom(-0.5f);
        sV.setValueTo(0.5f);
        tietY.setInputType(EDITOR_TYPE_NUM_DEC);
        tietU.setInputType(EDITOR_TYPE_NUM_DEC_SIGNED);
        tietV.setInputType(EDITOR_TYPE_NUM_DEC_SIGNED);
        tilY.setHint(R.string.y);
        tilU.setHint(R.string.u);
        tilV.setHint(R.string.v);
        sY.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietY.setText(String.valueOf(value)));
        sU.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietU.setText(String.valueOf(value)));
        sV.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietV.setText(String.valueOf(value)));
        tietY.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(0, s, sY));
        tietU.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(1, s, sU));
        tietV.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(2, s, sV));

        @Size(3) final float[] rgb = {Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor)};
        convert(rgb, yuv, RGB_TO_YUV);
        tietY.setText(String.valueOf(yuv[0]));
        tietU.setText(String.valueOf(yuv[1]));
        tietV.setText(String.valueOf(yuv[2]));
    }
}
