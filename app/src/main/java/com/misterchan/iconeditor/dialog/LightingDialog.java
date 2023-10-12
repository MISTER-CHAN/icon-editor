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
    private final float[] lighting;

    public LightingDialog(Context context) {
        this(context, null);
    }

    public LightingDialog(Context context, float[] defaultLighting) {
        builder = new MaterialAlertDialogBuilder(context)
                .setView(R.layout.channel_lighting);

        if (defaultLighting == null) {
            lighting = new float[]{1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f};
        } else {
            lighting = defaultLighting;
            for (int i = 0; i < lighting.length; i += 2) {
                lighting[i] = Math.min(Math.max(lighting[i], 0.0f), 2.0f);
                lighting[i + 1] = Math.min(Math.max(Math.round(lighting[i + 1]), -0xFF), 0xFF);
            }
        }
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

        final Slider sRedMul = dialog.findViewById(R.id.s_red_mul);
        final Slider sRedAdd = dialog.findViewById(R.id.s_red_add);
        final Slider sGreenMul = dialog.findViewById(R.id.s_green_mul);
        final Slider sGreenAdd = dialog.findViewById(R.id.s_green_add);
        final Slider sBlueMul = dialog.findViewById(R.id.s_blue_mul);
        final Slider sBlueAdd = dialog.findViewById(R.id.s_blue_add);
        final Slider sAlphaMul = dialog.findViewById(R.id.s_alpha_mul);
        final Slider sAlphaAdd = dialog.findViewById(R.id.s_alpha_add);

        sRedMul.setValue(lighting[0]);
        sRedAdd.setValue(lighting[1]);
        sGreenMul.setValue(lighting[2]);
        sGreenAdd.setValue(lighting[3]);
        sBlueMul.setValue(lighting[4]);
        sBlueAdd.setValue(lighting[5]);
        sAlphaMul.setValue(lighting[6]);
        sAlphaAdd.setValue(lighting[7]);

        final OnSliderChangeListener l = this::setElement;

        sRedMul.addOnChangeListener(l);
        sRedMul.addOnSliderTouchListener(l);
        sRedAdd.addOnChangeListener(l);
        sRedAdd.addOnSliderTouchListener(l);
        sGreenMul.addOnChangeListener(l);
        sGreenMul.addOnSliderTouchListener(l);
        sGreenAdd.addOnChangeListener(l);
        sGreenAdd.addOnSliderTouchListener(l);
        sBlueMul.addOnChangeListener(l);
        sBlueMul.addOnSliderTouchListener(l);
        sBlueAdd.addOnChangeListener(l);
        sBlueAdd.addOnSliderTouchListener(l);
        sAlphaMul.addOnChangeListener(l);
        sAlphaMul.addOnSliderTouchListener(l);
        sAlphaAdd.addOnChangeListener(l);
        sAlphaAdd.addOnSliderTouchListener(l);
    }
}
