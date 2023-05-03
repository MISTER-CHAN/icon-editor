package com.misterchan.iconeditor.listener;

import android.content.res.ColorStateList;

import androidx.annotation.NonNull;

import com.google.android.material.slider.RangeSlider;

import java.util.List;
import java.util.Objects;

public abstract class OnCircularRSChangeListener implements RangeSlider.OnChangeListener, RangeSlider.OnSliderTouchListener {
    private boolean canToggleByBounds = true;
    private boolean inclusive = true;
    private boolean togglingEnabled = true;

    public OnCircularRSChangeListener() {
    }

    public OnCircularRSChangeListener(boolean canSlideThruBound) {
        canToggleByBounds = canSlideThruBound;
    }

    public abstract void onChange(@NonNull RangeSlider slider, float value, boolean inclusive, boolean stopped);

    @Override
    public void onStartTrackingTouch(@NonNull RangeSlider slider) {
    }

    @Override
    public void onStopTrackingTouch(@NonNull RangeSlider slider) {
        onChange(slider, Float.NaN, inclusive, true);
    }

    @Override
    public final void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
        if (!fromUser) {
            return;
        }
        final List<Float> values = slider.getValues();
        if (canToggleByBounds && (value == slider.getValueFrom() || value == slider.getValueTo())
                || Objects.equals(values.get(0), values.get(values.size() - 1))) {
            if (togglingEnabled) {
                togglingEnabled = false;
                toggleInclusive(slider);
            }
        } else {
            togglingEnabled = true;
        }
        onChange(slider, value, inclusive, false);
    }

    private void toggleInclusive(@NonNull RangeSlider slider) {
        inclusive = !inclusive;
        final ColorStateList trackAtl = slider.getTrackActiveTintList(), trackItl = slider.getTrackInactiveTintList();
        slider.setTrackActiveTintList(trackItl);
        slider.setTrackInactiveTintList(trackAtl);
        final ColorStateList tickAtl = slider.getTickActiveTintList(), tickItl = slider.getTickInactiveTintList();
        slider.setTickActiveTintList(tickItl);
        slider.setTickInactiveTintList(tickAtl);
    }
}
