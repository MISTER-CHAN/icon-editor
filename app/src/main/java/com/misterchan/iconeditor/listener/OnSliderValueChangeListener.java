package com.misterchan.iconeditor.listener;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;

public interface OnSliderValueChangeListener extends Slider.OnChangeListener {
    void onValueChange(@NonNull Slider slider, float value);

    @Override
    default void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        if (fromUser) {
            onValueChange(slider, value);
        }
    }
}
