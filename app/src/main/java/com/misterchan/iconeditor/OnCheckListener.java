package com.misterchan.iconeditor;

import android.widget.CompoundButton;

interface OnCheckListener extends CompoundButton.OnCheckedChangeListener {

    void onCheck();

    @Override
    default void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            onCheck();
        }
    }
}
