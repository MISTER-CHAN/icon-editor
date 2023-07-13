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
import com.misterchan.iconeditor.ColorRange;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnCircularRSChangeListener;
import com.misterchan.iconeditor.listener.OnRSChangeListener;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

import java.util.List;

public class ColorRangeDialog {

    public interface OnChangedListener {
        void onChanged(ColorRange colorRange, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private final ColorRange cr;
    private OnChangedListener listener;

    public ColorRangeDialog(Context context) {
        this(context, null);
    }

    public ColorRangeDialog(Context context, ColorRange defaultColorRange) {
        builder = new MaterialAlertDialogBuilder(context)
                .setPositiveButton(R.string.ok, null)
                .setTitle(R.string.color_range)
                .setView(R.layout.color_range);

        cr = defaultColorRange != null ? defaultColorRange : new ColorRange();
    }

    public ColorRangeDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public ColorRangeDialog setOnPositiveButtonClickListener(OnChangedListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> listener.onChanged(cr, true));
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
        final Slider sTransition = dialog.findViewById(R.id.s_transition);
        final LabelFormatter dlf = value -> value + "°"; // Degree label formatter
        final LabelFormatter plf = value -> value * 100.0f + "%"; // Percentage label formatter

        rsHue.setValues(cr.cuboid[0], cr.cuboid[3]);
        rsSaturation.setValues(cr.cuboid[1], cr.cuboid[4]);
        rsValue.setValues(cr.cuboid[2], cr.cuboid[5]);
        sTransition.setValue(cr.transition);

        final OnCircularRSChangeListener hueOscl = new OnCircularRSChangeListener() {
            @Override
            public void onChange(@NonNull RangeSlider slider, float value, boolean inclusive, boolean stopped) {
                final List<Float> values = slider.getValues();
                cr.cuboid[0] = values.get(inclusive ? 0 : 1);
                cr.cuboid[3] = values.get(inclusive ? 1 : 0);
                cr.update();
                listener.onChanged(cr, stopped);
            }
        };
        if (cr.cuboid[0] > cr.cuboid[3]) {
            hueOscl.toggleInclusive(rsHue);
        }
        final OnRSChangeListener satOscl = (slider, stopped) -> {
            final List<Float> values = slider.getValues();
            cr.cuboid[1] = values.get(0);
            cr.cuboid[4] = values.get(1);
            cr.update();
            listener.onChanged(cr, stopped);
        };
        final OnRSChangeListener valOscl = (slider, stopped) -> {
            final List<Float> values = slider.getValues();
            cr.cuboid[2] = values.get(0);
            cr.cuboid[5] = values.get(1);
            cr.update();
            listener.onChanged(cr, stopped);
        };
        final OnSliderChangeListener transOscl = (slider, value, stopped) -> {
            cr.transition = value;
            listener.onChanged(cr, stopped);
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
        sTransition.addOnChangeListener(transOscl);
        sTransition.addOnSliderTouchListener(transOscl);
    }
}
