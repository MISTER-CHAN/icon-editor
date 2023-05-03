package com.misterchan.iconeditor.listener;

import androidx.annotation.NonNull;

import com.google.android.material.slider.RangeSlider;

public interface OnRSChangeListener extends RangeSlider.OnChangeListener, RangeSlider.OnSliderTouchListener {
    void onChange(@NonNull RangeSlider slider, boolean stopped);

    @Override
    default void onStartTrackingTouch(@NonNull RangeSlider slider) {
    }

    @Override
    default void onStopTrackingTouch(@NonNull RangeSlider slider) {
        onChange(slider, true);
    }

    @Override
    default void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
        if (fromUser) {
            onChange(slider, false);
        }
    }
}
