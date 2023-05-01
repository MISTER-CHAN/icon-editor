package com.misterchan.iconeditor.listener;

import androidx.annotation.IdRes;

import com.google.android.material.button.MaterialButtonToggleGroup;

public interface OnButtonCheckedListener extends MaterialButtonToggleGroup.OnButtonCheckedListener {
    void onButtonChecked(MaterialButtonToggleGroup group, @IdRes int checkedId);

    default void onButtonChecked(MaterialButtonToggleGroup group, @IdRes int checkedId, boolean isChecked) {
        if (isChecked) {
            onButtonChecked(group, checkedId);
        }
    }
}
