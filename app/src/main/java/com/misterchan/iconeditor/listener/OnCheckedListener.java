package com.misterchan.iconeditor.listener;

import android.widget.CompoundButton;

public interface OnCheckedListener extends CompoundButton.OnCheckedChangeListener {

    void onChecked();

    @Override
    default void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            onChecked();
        }
    }
}
