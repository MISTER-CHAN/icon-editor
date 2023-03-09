package com.misterchan.iconeditor.listener;

import android.text.Editable;
import android.text.TextWatcher;

public interface AfterTextChangedListener extends TextWatcher {

    void afterTextChanged(String s);

    @Override
    default void afterTextChanged(Editable s) {
        afterTextChanged(s.toString());
    }

    @Override
    default void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    default void onTextChanged(CharSequence s, int start, int before, int count) {
    }

}
