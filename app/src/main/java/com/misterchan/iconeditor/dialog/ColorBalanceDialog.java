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

public class ColorBalanceDialog {

    private final AlertDialog.Builder builder;
    private LightingDialog.OnLightingChangedListener listener;
    private Slider sRed, sGreen, sBlue;

    @Size(8)
    private final float[] lighting;

    public ColorBalanceDialog(Context context) {
        this(context, null);
    }

    public ColorBalanceDialog(Context context, float[] defaultLighting) {
        builder = new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_balance)
                .setTitle(R.string.color_balance)
                .setView(R.layout.color_balance);

        lighting = defaultLighting != null ? defaultLighting : new float[]{1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f};
    }

    private void onValueChange(Slider slider, float value, boolean stopped) {
        final float r = sRed.getValue(), g = sGreen.getValue(), b = sBlue.getValue();
        final float average = (r + g + b) / 3.0f;
        lighting[0] = 1.0f + r - average;
        lighting[2] = 1.0f + g - average;
        lighting[4] = 1.0f + b - average;
        listener.onChanged(lighting, stopped);
    }

    public ColorBalanceDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public ColorBalanceDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public ColorBalanceDialog setOnColorBalanceChangeListener(LightingDialog.OnLightingChangedListener listener) {
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

        sRed = dialog.findViewById(R.id.s_red);
        sGreen = dialog.findViewById(R.id.s_green);
        sBlue = dialog.findViewById(R.id.s_blue);

        sRed.setValue(lighting[0]);
        sGreen.setValue(lighting[2]);
        sBlue.setValue(lighting[4]);

        sRed.addOnChangeListener((OnSliderChangeListener) this::onValueChange);
        sRed.addOnSliderTouchListener((OnSliderChangeListener) this::onValueChange);
        sGreen.addOnChangeListener((OnSliderChangeListener) this::onValueChange);
        sGreen.addOnSliderTouchListener((OnSliderChangeListener) this::onValueChange);
        sBlue.addOnChangeListener((OnSliderChangeListener) this::onValueChange);
        sBlue.addOnSliderTouchListener((OnSliderChangeListener) this::onValueChange);
    }
}
