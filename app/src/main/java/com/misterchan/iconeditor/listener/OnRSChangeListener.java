package com.misterchan.iconeditor.listener;

import androidx.annotation.NonNull;

import com.google.android.material.slider.RangeSlider;

public interface OnRSChangeListener extends RangeSlider.OnChangeListener, RangeSlider.OnSliderTouchListener {
    void onChange(float value0, float value1, boolean stopped);

    @Override
    default void onStartTrackingTouch(@NonNull RangeSlider slider) {
    }

    @Override
    default void onStopTrackingTouch(@NonNull RangeSlider slider) {
        onChange(slider.getValues().get(0), slider.getValues().get(1), true);
    }

    @Override
    default void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
        if (fromUser) {
            onChange(slider.getValues().get(0), slider.getValues().get(1), false);
        }
    }
}
