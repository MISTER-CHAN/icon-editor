package com.misterchan.iconeditor;

import android.widget.SeekBar;

public abstract class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    private final boolean when;

    public OnSeekBarChangeListener(boolean when) {
        this.when = !when;
    }

    abstract void onChanged(SeekBar seekBar, int progress);

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (when && fromUser) {
            onChanged(seekBar, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (!when) {
            onChanged(seekBar, seekBar.getProgress());
        }
    }
}
