package com.misterchan.iconeditor;

import android.widget.SeekBar;

interface OnProgressChangeListener extends SeekBar.OnSeekBarChangeListener {

    void onProgressChanged(SeekBar seekBar, int progress);

    @Override
    default void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            onProgressChanged(seekBar, progress);
        }
    }

    @Override
    default void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    default void onStopTrackingTouch(SeekBar seekBar) {
    }
}
