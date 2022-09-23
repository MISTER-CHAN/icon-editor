package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class ColorRangeDialog {

    public interface OnColorRangeChangeListener {
        void onChange(int min, int max);
    }

    private final AlertDialog.Builder builder;
    private OnColorRangeChangeListener listener;

    private SeekBar sbMax;
    private SeekBar sbMin;

    public ColorRangeDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .setTitle(R.string.color_range)
                .setView(R.layout.color_range);
    }

    public ColorRangeDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        return this;
    }

    public ColorRangeDialog setOnPositiveButtonClickListener(final OnColorRangeChangeListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> listener.onChange(sbMin.getProgress(), sbMax.getProgress()));
        return this;
    }

    public ColorRangeDialog setOnColorRangeChangeListener(OnColorRangeChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public void show() {

        AlertDialog dialog = builder.show();

        android.view.Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        sbMax = dialog.findViewById(R.id.sb_max);
        sbMin = dialog.findViewById(R.id.sb_min);
        OnProgressChangeListener l = (OnProgressChangeListener) progress -> listener.onChange(sbMin.getProgress(), sbMax.getProgress());

        sbMax.setOnSeekBarChangeListener(l);
        sbMin.setOnSeekBarChangeListener(l);
    }
}
