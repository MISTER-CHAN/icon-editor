package com.misterchan.iconeditor.listener;

import android.widget.CompoundButton;

public interface OnCBCheckedListener extends CompoundButton.OnCheckedChangeListener {

    void onChecked(CompoundButton buttonView);

    @Override
    default void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            onChecked(buttonView);
        }
    }
}
