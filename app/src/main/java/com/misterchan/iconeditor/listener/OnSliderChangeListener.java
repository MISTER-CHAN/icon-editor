package com.misterchan.iconeditor.listener;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;

public interface OnSliderChangeListener extends Slider.OnChangeListener, Slider.OnSliderTouchListener {
    void onChange(float value, boolean stopped);

    @Override
    default void onStartTrackingTouch(@NonNull Slider slider) {
    }

    @Override
    default void onStopTrackingTouch(@NonNull Slider slider) {
        onChange(slider.getValue(), true);
    }

    @Override
    default void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        if (fromUser) {
            onChange(value, false);
        }
    }
}
