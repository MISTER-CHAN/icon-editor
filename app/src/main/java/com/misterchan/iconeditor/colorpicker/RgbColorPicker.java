package com.misterchan.iconeditor.colorpicker;

import android.graphics.Color;
import android.graphics.ColorSpace;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.databinding.ColorPickerRgbBinding;
import com.misterchan.iconeditor.listener.OnItemSelectedListener;
import com.misterchan.iconeditor.util.ColorUtils;

public class RgbColorPicker extends ColorPicker {
    public interface OnColorPickerChangeListener {
        void onChange();
    }

    private final boolean colorRep;
    public final ColorPickerRgbBinding binding;
    private final ColorSpace.Connector connectorToSrgb;

    @Size(3)
    private final float[] rgb;

    @ColorInt
    private int colorInt;

    @ColorLong
    private long color;

    public RgbColorPicker(long color, ColorPickerRgbBinding binding, OnColorPickerChangeListener cpl) {
        colorRep = Settings.INST.colorRep();
        ColorSpace colorSpace = Color.colorSpace(color);
        connectorToSrgb = ColorSpace.connect(colorSpace);
        float c0Min, c0Max, c1Min, c1Max, c2Min, c2Max;
        if (colorRep) {
            c0Min = colorSpace.getMinValue(0);
            c0Max = colorSpace.getMaxValue(0);
            c1Min = colorSpace.getMinValue(1);
            c1Max = colorSpace.getMaxValue(1);
            c2Min = colorSpace.getMinValue(2);
            c2Max = colorSpace.getMaxValue(2);
        } else {
            c0Min = c1Min = c2Min = 0.0f;
            c0Max = c1Max = c2Max = 255.0f;
        }
        int radix = colorRep ? 10 : Settings.INST.colorIntCompRadix();
        int inputType = colorRep ? EDITOR_TYPE_NUM_DEC_SIGNED : EDITOR_TYPE_NUM;
        prop = new Properties(colorRep, "R", "G", "B",
                c0Min, c0Max, c1Min, c1Max, c2Min, c2Max, inputType, inputType, inputType,
                radix == 10);

        this.binding = binding;
        binding.sColorIntCompNumSysLabel.setVisibility(colorRep ? View.GONE : View.VISIBLE);
        binding.sColorIntCompNumSys.setVisibility(colorRep ? View.GONE : View.VISIBLE);
        binding.tvColorSpaceLabel.setVisibility(colorRep ? View.VISIBLE : View.GONE);
        binding.tvColorSpace.setVisibility(colorRep ? View.VISIBLE : View.GONE);
        if (colorRep) {
            binding.tvColorSpace.setText(colorSpace.toString());
        } else {
            binding.sColorIntCompNumSys.setSelection(Settings.INST.colorIntCompRadix() <= 10 ? 1 : 0);
        }
        if (cpl != null) {
            binding.sColorRep.setSelection(colorRep ? 1 : 0);
            binding.sColorRep.setOnItemSelectedListener((OnItemSelectedListener) (parent, view, position, id) -> {
                Settings.INST.pref().edit().putBoolean(Settings.KEY_CR, position == 1).apply();
                Settings.INST.update(Settings.KEY_CR);
                cpl.onChange();
            });
            binding.sColorIntCompNumSys.setOnItemSelectedListener((OnItemSelectedListener) (parent, view, position, id) -> {
                Settings.INST.pref().edit().putInt(Settings.KEY_CIR, position == 1 ? 10 : 16).apply();
                Settings.INST.update(Settings.KEY_CIR);
                cpl.onChange();
            });
        }

        this.color = color;
        colorInt = Color.toArgb(color);
        rgb = colorRep ? new float[]{Color.red(color), Color.green(color), Color.blue(color)} : null;
    }

    @Override
    public long color() {
        return color;
    }

    @Override
    public int colorInt() {
        return colorInt;
    }

    @Override
    public float getComponent(int index) {
        return colorRep ? rgb[index] : colorInt >> (2 - index) * 8 & 0xFF;
    }

    @Override
    public void setAlpha(float a) {
        if (colorRep) {
            color = ColorUtils.setAlpha(color, a);
        } else {
            colorInt = ColorUtils.setAlpha(colorInt, (int) a);
        }
        setColor();
    }

    @Override
    public void setComponent(@IntRange(from = 0, to = 2) int index, float c) {
        if (colorRep) {
            if (rgb[index] == c) return;
            rgb[index] = c;
            color = ColorUtils.setComponent(color, index, c);
        } else {
            colorInt = ColorUtils.setComponent(colorInt, index, (int) c);
        }
        setColor();
    }

    private void setColor() {
        if (colorRep) {
            colorInt = ColorUtils.convert(rgb[0], rgb[1], rgb[2], Color.alpha(color), connectorToSrgb);
        } else {
            color = Color.pack(colorInt);
        }
    }
}
