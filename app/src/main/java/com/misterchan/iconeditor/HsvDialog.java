package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

class HsvDialog {

    public interface OnHsvChangeListener {
        void onChange(@Size(3) float[] deltaHsv, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private OnHsvChangeListener listener;

    @Size(3)
    private float[] deltaHsv = new float[3];

    public HsvDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.hsv)
                .setView(R.layout.hsv);
    }

    public HsvDialog setDefaultDeltaHsv(float[] deltaHsv) {
        this.deltaHsv = deltaHsv;
        return this;
    }

    public HsvDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public HsvDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public HsvDialog setOnHsvChangeListener(OnHsvChangeListener listener) {
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

        final SeekBar sbHue = dialog.findViewById(R.id.sb_hue);
        final SeekBar sbSaturation = dialog.findViewById(R.id.sb_saturation);
        final SeekBar sbValue = dialog.findViewById(R.id.sb_value);

        sbHue.setProgress((int) deltaHsv[0]);
        sbHue.setOnSeekBarChangeListener((OnSeekBarChangeListener) (progress, stopped) -> {
            deltaHsv[0] = progress;
            listener.onChange(deltaHsv, stopped);
        });

        sbSaturation.setProgress((int) (deltaHsv[1] * 100.0f));
        sbSaturation.setOnSeekBarChangeListener((OnSeekBarChangeListener) (progress, stopped) -> {
            deltaHsv[1] = progress / 100.0f;
            listener.onChange(deltaHsv, stopped);
        });

        sbValue.setProgress((int) (deltaHsv[2] * 100.0f));
        sbValue.setOnSeekBarChangeListener((OnSeekBarChangeListener) (progress, stopped) -> {
            deltaHsv[2] = progress / 100.0f;
            listener.onChange(deltaHsv, stopped);
        });
    }
}
