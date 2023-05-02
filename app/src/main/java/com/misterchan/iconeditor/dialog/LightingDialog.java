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

public class LightingDialog {

    public interface OnLightingChangedListener {
        void onChanged(@Size(8) float[] lighting, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private OnLightingChangedListener onLightingChangeListener;

    @Size(8)
    private final float[] lighting = new float[]{1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f};

    public LightingDialog(Context context) {
        builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.channel_lighting)
                .setView(R.layout.channel_lighting);
    }

    private void setElement(Slider slider, float e, boolean stopped) {
        setElement(slider.getTag().toString().charAt(0) - '0', e, stopped);
    }

    private void setElement(int index, float e, boolean stopped) {
        lighting[index] = e;
        onLightingChangeListener.onChanged(lighting, stopped);
    }

    public LightingDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel,
                (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public LightingDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public LightingDialog setOnLightingChangeListener(OnLightingChangedListener listener) {
        onLightingChangeListener = listener;
        return this;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final Slider sRedScale = dialog.findViewById(R.id.s_red_scale);
        final Slider sRedShift = dialog.findViewById(R.id.s_red_shift);
        final Slider sGreenScale = dialog.findViewById(R.id.s_green_scale);
        final Slider sGreenShift = dialog.findViewById(R.id.s_green_shift);
        final Slider sBlueScale = dialog.findViewById(R.id.s_blue_scale);
        final Slider sBlueShift = dialog.findViewById(R.id.s_blue_shift);
        final Slider sAlphaScale = dialog.findViewById(R.id.s_alpha_scale);
        final Slider sAlphaShift = dialog.findViewById(R.id.s_alpha_shift);

        final OnSliderChangeListener l = this::setElement;

        sRedScale.addOnChangeListener(l);
        sRedScale.addOnSliderTouchListener(l);
        sRedShift.addOnChangeListener(l);
        sRedShift.addOnSliderTouchListener(l);
        sGreenScale.addOnChangeListener(l);
        sGreenScale.addOnSliderTouchListener(l);
        sGreenShift.addOnChangeListener(l);
        sGreenShift.addOnSliderTouchListener(l);
        sBlueScale.addOnChangeListener(l);
        sBlueScale.addOnSliderTouchListener(l);
        sBlueShift.addOnChangeListener(l);
        sBlueShift.addOnSliderTouchListener(l);
        sAlphaScale.addOnChangeListener(l);
        sAlphaScale.addOnSliderTouchListener(l);
        sAlphaShift.addOnChangeListener(l);
        sAlphaShift.addOnSliderTouchListener(l);
    }
}
