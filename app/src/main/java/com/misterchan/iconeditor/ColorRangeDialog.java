package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

class ColorRangeDialog {

    public interface OnColorRangeChangeListener {
        void onChange(int hueMin, int hueMax, int lumMin, int lumMax, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private OnColorRangeChangeListener listener;

    private SeekBar sbHueMin, sbHueMax;
    private SeekBar sbLumMin, sbLumMax;

    public ColorRangeDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .setTitle(R.string.color_range)
                .setView(R.layout.color_range);
    }

    public ColorRangeDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public ColorRangeDialog setOnPositiveButtonClickListener(OnColorRangeChangeListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) ->
                listener.onChange(sbHueMin.getProgress(), sbHueMax.getProgress(),
                        sbLumMin.getProgress(), sbLumMax.getProgress(),
                        true));
        return this;
    }

    public ColorRangeDialog setOnColorRangeChangeListener(OnColorRangeChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public void show() {

        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        sbHueMin = dialog.findViewById(R.id.sb_hue_min);
        sbHueMax = dialog.findViewById(R.id.sb_hue_max);
        sbLumMin = dialog.findViewById(R.id.sb_luminance_min);
        sbLumMax = dialog.findViewById(R.id.sb_luminance_max);
        final OnSeekBarChangeListener l = (progress, stopped) ->
                listener.onChange(sbHueMin.getProgress(), sbHueMax.getProgress(),
                        sbLumMin.getProgress(), sbLumMax.getProgress(),
                        stopped);

        sbHueMin.setOnSeekBarChangeListener(l);
        sbHueMax.setOnSeekBarChangeListener(l);

        sbLumMin.setOnSeekBarChangeListener((OnSeekBarChangeListener) (progress, stopped) -> {
            if (progress > sbLumMax.getProgress()) {
                sbLumMin.setProgress(sbLumMax.getProgress());
            }
            l.onChanged(progress, stopped);
        });

        sbLumMax.setOnSeekBarChangeListener((OnSeekBarChangeListener) (progress, stopped) -> {
            if (progress < sbLumMin.getProgress()) {
                sbLumMax.setProgress(sbLumMin.getProgress());
            }
            l.onChanged(progress, stopped);
        });
    }
}
