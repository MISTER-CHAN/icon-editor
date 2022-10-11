package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
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
    @Size(3)
    private final float[] deltaHSV = new float[3];
    private OnHSVChangeListener listener;

    public HSVDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.hsv)
                .setView(R.layout.hsv);
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

    public HSVDialog setOnHSVChangeListener(OnHSVChangeListener listener, boolean when) {
        this.when = when;
        return setOnHSVChangeListener(listener);
    }

    public void show() {
        AlertDialog dialog = builder.show();

        android.view.Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        ((SeekBar) dialog.findViewById(R.id.sb_hue))
                .setOnSeekBarChangeListener(new OnSeekBarChangeListener(when) {
                    @Override
                    void onChanged(SeekBar seekBar, int progress) {
                        deltaHSV[0] = progress;
                        listener.onChange(deltaHSV);
                    }
                });

        ((SeekBar) dialog.findViewById(R.id.sb_saturation))
                .setOnSeekBarChangeListener(new OnSeekBarChangeListener(when) {
                    @Override
                    void onChanged(SeekBar seekBar, int progress) {
                        deltaHSV[1] = progress / 100.0f;
                        listener.onChange(deltaHSV);
                    }
                });

        ((SeekBar) dialog.findViewById(R.id.sb_value))
                .setOnSeekBarChangeListener(new OnSeekBarChangeListener(when) {
                    @Override
                    void onChanged(SeekBar seekBar, int progress) {
                        deltaHSV[2] = progress / 100.0f;
                        listener.onChange(deltaHSV);
                    }
                });
    }
}
