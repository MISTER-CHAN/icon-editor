package com.misterchan.iconeditor.listener;

import android.widget.SeekBar;

public interface OnSBChangeListener extends SeekBar.OnSeekBarChangeListener {

    void onChange(SeekBar seekBar, int progress, boolean stopped);

    @Override
    default void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            onChange(seekBar, progress, false);
        }
    }

    @Override
    default void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    default void onStopTrackingTouch(SeekBar seekBar) {
        onChange(seekBar, seekBar.getProgress(), true);
    }
}
