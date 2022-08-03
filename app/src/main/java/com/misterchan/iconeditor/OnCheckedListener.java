package com.misterchan.iconeditor;

import android.widget.CompoundButton;

public interface OnCheckedListener extends CompoundButton.OnCheckedChangeListener {

    void onCheckedChange();

    @Override
    default void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            onCheckedChange();
        }
    }
}
