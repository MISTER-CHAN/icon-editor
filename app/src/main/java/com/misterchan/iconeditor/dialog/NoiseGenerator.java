package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnCBCheckedListener;
import com.misterchan.iconeditor.listener.OnSBChangeListener;

public class NoiseGenerator {

    private static final int SB_MAX = 100;

    public enum WhatToDraw {
        PIXEL, POINT, REF
    }

    public interface OnPropChangedListener {
        void onChanged(Properties properties, boolean stopped);
    }

    public static class Properties {
        private boolean noRepeats = false;
        private float noisiness = 0.0f;
        private Long seed = null;
        private WhatToDraw whatToDraw = WhatToDraw.PIXEL;

        public boolean noRepeats() {
            return noRepeats;
        }

        public float noisy() {
            return noisiness;
        }

        public Long seed() {
            return seed;
        }

        public WhatToDraw whatToDraw() {
            return whatToDraw;
        }
    }

    private final AlertDialog.Builder builder;
    private OnPropChangedListener listener;
    private final Properties properties = new Properties();

    public NoiseGenerator(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.generate_noise)
                .setView(R.layout.noise_generator);
    }

    public NoiseGenerator setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public NoiseGenerator setOnConfirmListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public NoiseGenerator setOnPropChangedListener(OnPropChangedListener listener) {
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

        final CheckBox cbNoRepeats = dialog.findViewById(R.id.cb_no_repeats);
        final RadioButton rbPixel = dialog.findViewById(R.id.rb_pixel);
        final RadioButton rbPoint = dialog.findViewById(R.id.rb_point);
        final RadioButton rbRef = dialog.findViewById(R.id.rb_ref);
        final SeekBar sbNoisy = dialog.findViewById(R.id.sb_noisy);
        final TextInputEditText tietSeed = dialog.findViewById(R.id.tiet_seed);

        sbNoisy.setMax(SB_MAX);

        cbNoRepeats.setOnCheckedChangeListener((buttonView, isChecked) -> {
            properties.noRepeats = isChecked;
            update(true);
        });
        rbPixel.setOnCheckedChangeListener((OnCBCheckedListener) () -> {
            properties.whatToDraw = WhatToDraw.PIXEL;
            update(true);
        });
        rbPoint.setOnCheckedChangeListener((OnCBCheckedListener) () -> {
            properties.whatToDraw = WhatToDraw.POINT;
            update(true);
        });
        rbRef.setOnCheckedChangeListener((OnCBCheckedListener) () -> {
            properties.whatToDraw = WhatToDraw.REF;
            update(true);
        });
        sbNoisy.setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> {
            properties.noisiness = (float) progress / (float) SB_MAX;
            update(stopped);
        });
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
