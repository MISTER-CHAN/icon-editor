package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

public class HsvDialog {

    public interface OnHsvChangedListener {
        void onChanged(@Size(3) float[] deltaHsv, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private OnHsvChangedListener listener;

    @Size(3)
    private float[] deltaHsv = new float[3];

    public HsvDialog(Context context) {
        builder = new MaterialAlertDialogBuilder(context)
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

    public HsvDialog setOnHsvChangeListener(OnHsvChangedListener listener) {
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

        final Slider sHue = dialog.findViewById(R.id.s_hue);
        final Slider sSaturation = dialog.findViewById(R.id.s_saturation);
        final Slider sValue = dialog.findViewById(R.id.s_value);

        final OnSliderChangeListener l = this::update;

        sHue.setValue(deltaHsv[0]);
        sSaturation.setValue(deltaHsv[1]);
        sValue.setValue(deltaHsv[2]);
        sHue.addOnChangeListener(l);
        sHue.addOnSliderTouchListener(l);
        sSaturation.addOnChangeListener(l);
        sSaturation.addOnSliderTouchListener(l);
        sValue.addOnChangeListener(l);
        sValue.addOnSliderTouchListener(l);
    }

    private void update(Slider slider, float value, boolean stopped) {
        deltaHsv[slider.getTag().toString().charAt(0) - '0'] = value;
        listener.onChanged(deltaHsv, stopped);
    }
}
