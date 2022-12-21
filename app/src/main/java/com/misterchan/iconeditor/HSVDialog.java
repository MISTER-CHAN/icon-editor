package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

public class HSVDialog {

    public interface OnHSVChangeListener {
        void onChange(@Size(3) float[] deltaHSV);
    }

    private final AlertDialog.Builder builder;
    private boolean when = false;
    private OnHSVChangeListener listener;

    @Size(3)
    private float[] deltaHSV = new float[3];

    public HSVDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.hsv)
                .setView(R.layout.hsv);
    }

    public HSVDialog setDefaultDeltaHSV(float[] deltaHSV) {
        this.deltaHSV = deltaHSV;
        return this;
    }

    public HSVDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public HSVDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public HSVDialog setOnHSVChangeListener(OnHSVChangeListener listener) {
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

        sbHue.setProgress((int) deltaHSV[0]);
        sbHue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> {
            deltaHSV[0] = progress;
            listener.onChange(deltaHSV);
        });

        sbSaturation.setProgress((int) deltaHSV[1] * 100);
        sbSaturation.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> {
            deltaHSV[1] = progress / 100.0f;
            listener.onChange(deltaHSV);
        });

        sbValue.setProgress((int) deltaHSV[2] * 100);
        sbValue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> {
            deltaHSV[2] = progress / 100.0f;
            listener.onChange(deltaHSV);
        });
    }
}
