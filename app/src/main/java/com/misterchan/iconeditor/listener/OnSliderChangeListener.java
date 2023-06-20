package com.misterchan.iconeditor.listener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;

public interface OnSliderChangeListener extends Slider.OnChangeListener, Slider.OnSliderTouchListener {
    void onChange(@Nullable Slider slider, float value, boolean stopped);

    @Override
    default void onStartTrackingTouch(@NonNull Slider slider) {
    }

    @Override
    default void onStopTrackingTouch(@NonNull Slider slider) {
        onChange(slider, slider.getValue(), true);
    }

    @Override
    default void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        if (fromUser) {
            onChange(slider, value, false);
        }
    }
}
