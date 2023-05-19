package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnCircularRSChangeListener;
import com.misterchan.iconeditor.listener.OnRSChangeListener;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

import java.util.List;

public class ColorRangeDialog {

    public interface OnChangedListener {
        void onChanged(float[] cuboid, float tolerance, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private float tolerance;
    private OnChangedListener listener;

    @Size(6)
    private final float[] cuboid = new float[]{
            0.0f, 0.0f, 0.0f, 360.0f, 1.0f, 1.0f
    };

    public ColorRangeDialog(Context context) {
        builder = new MaterialAlertDialogBuilder(context)
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
        builder.setPositiveButton(R.string.ok, (dialog, which) -> listener.onChanged(cuboid, tolerance, true));
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

        final RangeSlider rsHue = dialog.findViewById(R.id.rs_hue);
        final RangeSlider rsSaturation = dialog.findViewById(R.id.rs_saturation);
        final RangeSlider rsValue = dialog.findViewById(R.id.rs_value);
        final Slider sTolerance = dialog.findViewById(R.id.s_tolerance);
        final LabelFormatter dlf = value -> value + "°"; // Degree label formatter
        final LabelFormatter plf = value -> value * 100.0f + "%"; // Percentage label formatter

        final OnCircularRSChangeListener hueOscl = new OnCircularRSChangeListener() {
            @Override
            public void onChange(@NonNull RangeSlider slider, float value, boolean inclusive, boolean stopped) {
                final List<Float> values = slider.getValues();
                cuboid[0] = values.get(inclusive ? 0 : 1);
                cuboid[3] = values.get(inclusive ? 1 : 0);
                listener.onChanged(cuboid, tolerance, stopped);
            }
        };
        final OnRSChangeListener satOscl = (slider, stopped) -> {
            final List<Float> values = slider.getValues();
            cuboid[1] = values.get(0);
            cuboid[4] = values.get(1);
            listener.onChanged(cuboid, tolerance, stopped);
        };
        final OnRSChangeListener valOscl = (slider, stopped) -> {
            final List<Float> values = slider.getValues();
            cuboid[2] = values.get(0);
            cuboid[5] = values.get(1);
            listener.onChanged(cuboid, tolerance, stopped);
        };
        final OnSliderChangeListener tolOscl = (slider, value, stopped) -> {
            tolerance = value;
            listener.onChanged(cuboid, tolerance, stopped);
        };

        rsHue.addOnChangeListener(hueOscl);
        rsHue.addOnSliderTouchListener(hueOscl);
        rsHue.setLabelFormatter(dlf);
        rsSaturation.addOnChangeListener(satOscl);
        rsSaturation.addOnSliderTouchListener(satOscl);
        rsSaturation.setLabelFormatter(plf);
        rsValue.addOnChangeListener(valOscl);
        rsValue.addOnSliderTouchListener(valOscl);
        rsValue.setLabelFormatter(plf);
        sTolerance.addOnChangeListener(tolOscl);
        sTolerance.addOnSliderTouchListener(tolOscl);
    }
}
