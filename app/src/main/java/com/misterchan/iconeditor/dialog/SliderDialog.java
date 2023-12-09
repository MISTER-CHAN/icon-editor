package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

public class SliderDialog extends FilterDialog {

    private float stepSize, valueTo, valueFrom, value;
    private OnSliderChangeListener listener;
    private Slider slider;

    public SliderDialog(Context context) {
        super(context);
        builder.setView(R.layout.slider);
    }

    @Override
    void onFilterCommit() {
        listener.onChange(slider, slider.getValue(), true);
    }

    public SliderDialog setIcon(@DrawableRes int iconId) {
        builder.setIcon(iconId);
        return this;
    }

    public SliderDialog setIcon(@Nullable Drawable drawable) {
        builder.setIcon(drawable);
        return this;
    }

    public SliderDialog setOnChangeListener(OnSliderChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public SliderDialog setStepSize(float stepSize) {
        this.stepSize = stepSize;
        return this;
    }

    public SliderDialog setTitle(@StringRes int titleId) {
        builder.setTitle(titleId);
        return this;
    }

    public SliderDialog setValue(float value) {
        this.value = value;
        return this;
    }

    public SliderDialog setValueFrom(float valueFrom) {
        this.valueFrom = valueFrom;
        return this;
    }

    public SliderDialog setValueTo(float valueTo) {
        this.valueTo = valueTo;
        return this;
    }

    @Override
    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        slider = dialog.findViewById(R.id.slider);
        slider.setStepSize(stepSize);
        slider.setValueTo(valueTo);
        slider.setValueFrom(valueFrom);
        slider.setValue(value);
        slider.addOnChangeListener(listener);
        slider.addOnSliderTouchListener(listener);
    }
}
