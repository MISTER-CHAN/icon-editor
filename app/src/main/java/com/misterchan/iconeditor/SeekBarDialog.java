package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

class SeekBarDialog {

    private final AlertDialog.Builder builder;
    private int max, min, progress;
    private OnSeekBarChangeListener onChangeListener;

    public SeekBarDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setView(R.layout.seek_bar);
    }

    public SeekBarDialog setMax(int max) {
        this.max = max;
        return this;
    }

    public SeekBarDialog setMin(int min) {
        this.min = min;
        return this;
    }

    public SeekBarDialog setOnApplyListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public SeekBarDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        return setOnCancelListener(listener, true);
    }

    public SeekBarDialog setOnCancelListener(DialogInterface.OnCancelListener listener, boolean showButton) {
        builder.setOnCancelListener(listener);
        if (showButton) {
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        }
        return this;
    }

    public SeekBarDialog setOnChangeListener(OnSeekBarChangeListener listener) {
        onChangeListener = listener;
        return this;
    }

    public SeekBarDialog setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public SeekBarDialog setTitle(@StringRes int titleId) {
        builder.setTitle(titleId);
        return this;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final SeekBar seekBar = dialog.findViewById(R.id.seek_bar);
        seekBar.setMax(max);
        seekBar.setMin(min);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(onChangeListener);
    }
}
