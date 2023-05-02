package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

public class SliderDialog {

    private final AlertDialog.Builder builder;
    private float stepSize, valueTo, valueFrom, value;
    private OnSliderChangeListener onChangeListener;

    public SliderDialog(Context context) {
        builder = new MaterialAlertDialogBuilder(context)
                .setView(R.layout.slider);
    }

    public SliderDialog setOnApplyListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public SliderDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        return setOnCancelListener(listener, true);
    }

    public SliderDialog setOnCancelListener(DialogInterface.OnCancelListener listener, boolean showButton) {
        builder.setOnCancelListener(listener);
        if (showButton) {
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        }
        return this;
    }

    public SliderDialog setOnChangeListener(OnSliderChangeListener listener) {
        onChangeListener = listener;
        return this;
    }

    public SliderDialog setStepSize(float stepSize) {
        this.stepSize = stepSize;
        return this;
    }

    public SliderDialog setTitle(@StringRes int titleId) {
        builder.setTitle(titleId);
        return this;
    }

    public SliderDialog setValue(float value) {
        this.value = value;
        return this;
    }

    public SliderDialog setValueFrom(float valueFrom) {
        this.valueFrom = valueFrom;
        return this;
    }

    public SliderDialog setValueTo(float valueTo) {
        this.valueTo = valueTo;
        return this;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final Slider slider = dialog.findViewById(R.id.slider);
        slider.setStepSize(stepSize);
        slider.setValueTo(valueTo);
        slider.setValueFrom(valueFrom);
        slider.setValue(value);
        slider.addOnChangeListener(onChangeListener);
        slider.addOnSliderTouchListener(onChangeListener);
    }
}
