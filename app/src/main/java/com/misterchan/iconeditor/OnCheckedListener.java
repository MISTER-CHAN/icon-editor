package com.misterchan.iconeditor;

import android.widget.CompoundButton;

interface OnCheckedListener extends CompoundButton.OnCheckedChangeListener {

    void onChecked();

    @Override
    default void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            onChecked();
        }
    }
}
