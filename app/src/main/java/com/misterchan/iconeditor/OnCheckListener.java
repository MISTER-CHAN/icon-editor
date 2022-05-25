package com.misterchan.iconeditor;

import android.widget.CompoundButton;

interface OnCheckListener extends CompoundButton.OnCheckedChangeListener {

    void onCheck(boolean isChecked);

    @Override
    default void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        onCheck(isChecked);
    }
}
