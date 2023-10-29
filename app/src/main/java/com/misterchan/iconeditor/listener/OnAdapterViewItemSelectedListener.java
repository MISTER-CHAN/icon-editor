package com.misterchan.iconeditor.listener;

import android.widget.AdapterView;

public interface OnAdapterViewItemSelectedListener extends AdapterView.OnItemSelectedListener {
    @Override
    default void onNothingSelected(AdapterView<?> parent) {
    }
}
