package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class ColorRangeDialog {

    public interface OnColorRangeChangeListener {
        void onChange(int hueMin, int hueMax, int valueMin, int valueMax);
    }

    private final AlertDialog.Builder builder;
    private OnColorRangeChangeListener listener;

    private SeekBar sbHueMin, sbHueMax;
    private SeekBar sbValueMin, sbValueMax;

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
                        sbValueMin.getProgress(), sbValueMax.getProgress()));
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
        sbValueMin = dialog.findViewById(R.id.sb_value_min);
        sbValueMax = dialog.findViewById(R.id.sb_value_max);
        final OnSeekBarProgressChangeListener l = (seekBar, progress) ->
                listener.onChange(sbHueMin.getProgress(), sbHueMax.getProgress(),
                        sbValueMin.getProgress(), sbValueMax.getProgress());

        sbHueMin.setOnSeekBarChangeListener(l);
        sbHueMax.setOnSeekBarChangeListener(l);

        sbValueMin.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> {
            if (progress > sbValueMax.getProgress()) {
                seekBar.setProgress(sbValueMax.getProgress());
            }
            l.onProgressChanged(seekBar, progress);
        });

        sbValueMax.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> {
            if (progress < sbValueMin.getProgress()) {
                seekBar.setProgress(sbValueMin.getProgress());
            }
            l.onProgressChanged(seekBar, progress);
        });
    }
}
