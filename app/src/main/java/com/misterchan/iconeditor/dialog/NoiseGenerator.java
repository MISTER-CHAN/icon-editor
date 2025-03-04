package com.misterchan.iconeditor.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnButtonCheckedListener;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

public class NoiseGenerator extends FilterDialog {

    private static final int SB_MAX = 100;

    public enum DrawingPrimitive {
        PIXEL, POINT, CLIP
    }

    public interface OnPropChangedListener {
        void onChanged(Properties properties, boolean stopped);
    }

    public static class Properties {
        private boolean noRepeats = false;
        private DrawingPrimitive drawingPrimitive = DrawingPrimitive.PIXEL;
        private float noisiness = 0.0f;
        private Long seed = null;

        public DrawingPrimitive drawingPrimitive() {
            return drawingPrimitive;
        }

        public boolean noRepeats() {
            return noRepeats;
        }

        public float noisiness() {
            return noisiness;
        }

        public Long seed() {
            return seed;
        }
    }

    private final OnPropChangedListener listener;
    private final Properties properties = new Properties();

    public NoiseGenerator(Context context, OnPropChangedListener listener) {
        super(context);
        builder.setTitle(R.string.generate_noise).setView(R.layout.noise_generator);

        this.listener = listener;
    }

    @Override
    void onFilterCommit() {
        listener.onChanged(properties, true);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final CheckBox cbNoRepeats = dialog.findViewById(R.id.cb_no_repeats);
        final MaterialButtonToggleGroup btgDrawingPrimitive = dialog.findViewById(R.id.btg_drawing_primitives);
        final Slider sNoisiness = dialog.findViewById(R.id.s_noisiness);
        final TextInputEditText tietSeed = dialog.findViewById(R.id.tiet_seed);

        sNoisiness.setLabelFormatter(value -> value + "%");
        sNoisiness.setValueTo(SB_MAX);

        btgDrawingPrimitive.addOnButtonCheckedListener((OnButtonCheckedListener) (group, checkedId) -> {
            properties.drawingPrimitive = switch (checkedId) {
                case R.id.b_pixel -> DrawingPrimitive.PIXEL;
                case R.id.b_point -> DrawingPrimitive.POINT;
                case R.id.b_clip -> DrawingPrimitive.CLIP;
                default -> null;
            };
            update(true);
        });
        cbNoRepeats.setOnCheckedChangeListener((buttonView, isChecked) -> {
            properties.noRepeats = isChecked;
            update(true);
        });
        {
            final OnSliderChangeListener l = (slider, value, stopped) -> {
                properties.noisiness = value / (float) SB_MAX;
                update(stopped);
            };
            sNoisiness.addOnChangeListener(l);
            sNoisiness.addOnSliderTouchListener(l);
        }
        tietSeed.addTextChangedListener((AfterTextChangedListener) s -> {
            try {
                properties.seed = Long.parseLong(s);
            } catch (NumberFormatException e) {
                properties.seed = null;
            }
            update(true);
        });
    }

    public void update(boolean stopped) {
        listener.onChanged(properties, stopped);
    }
}
