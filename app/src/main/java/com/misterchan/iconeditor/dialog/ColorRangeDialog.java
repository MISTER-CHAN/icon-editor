package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.listener.OnRSChangeListener;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

import java.util.List;

public class ColorRangeDialog {

    public interface OnChangedListener {
        void onChanged(float hMin, float hMax, float sMin, float sMax, float vMin, float vMax, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private OnChangedListener listener;

    private Slider sHueMin, sHueMax;
    private RangeSlider rsSaturation, rsValue;

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

    public ColorRangeDialog setOnPositiveButtonClickListener(OnChangedListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> update(listener, true));
        return this;
    }

    public ColorRangeDialog setOnColorRangeChangeListener(OnChangedListener listener) {
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

        sHueMin = dialog.findViewById(R.id.s_hue_min);
        sHueMax = dialog.findViewById(R.id.s_hue_max);
        rsSaturation = dialog.findViewById(R.id.rs_saturation);
        rsValue = dialog.findViewById(R.id.rs_value);
        final LabelFormatter dlf = value -> value + "°"; // Degree label formatter
        final LabelFormatter plf = value -> value + "%"; // Percentage label formatter
        final OnSliderChangeListener sl = (slider, value, stopped) -> update(listener, stopped);
        final OnRSChangeListener rsl = (slider, value0, value1, stopped) -> update(listener, stopped);

        sHueMin.addOnChangeListener(sl);
        sHueMin.addOnSliderTouchListener(sl);
        sHueMin.setLabelFormatter(dlf);
        sHueMax.addOnChangeListener(sl);
        sHueMax.addOnSliderTouchListener(sl);
        sHueMax.setLabelFormatter(dlf);
        rsSaturation.addOnChangeListener(rsl);
        rsSaturation.addOnSliderTouchListener(rsl);
        rsSaturation.setLabelFormatter(plf);
        rsValue.addOnChangeListener(rsl);
        rsValue.addOnSliderTouchListener(rsl);
        rsValue.setLabelFormatter(plf);
    }

    private void update(OnChangedListener listener, boolean stopped) {
        final List<Float> s = rsSaturation.getValues(), v = rsValue.getValues();
        listener.onChanged(sHueMin.getValue(), sHueMax.getValue(),
                s.get(0) / 100.0f, s.get(1) / 100.0f,
                v.get(0) / 100.0f, v.get(1) / 100.0f,
                stopped);
    }
}
