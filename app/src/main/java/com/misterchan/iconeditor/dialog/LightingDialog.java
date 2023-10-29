package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.colorpicker.RgbColorPicker;

public class LightingDialog {

    public interface OnLightingChangedListener {
        void onChanged(@Size(8) float[] lighting, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private final Context context;
    private OnLightingChangedListener onLightingChangeListener;

    @Size(8)
    private final float[] lighting;

    public LightingDialog(Context context) {
        this(context, null);
    }

    public LightingDialog(Context context, float[] defaultLighting) {
        this.context = context;
        builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.lighting)
                .setView(R.layout.lighting);

        if (defaultLighting == null) {
            lighting = new float[]{1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f};
        } else {
            lighting = defaultLighting;
            for (int i = 0; i < lighting.length; i += 2) {
                lighting[i] = Math.min(Math.max(lighting[i], 0.0f), 1.0f);
                lighting[i + 1] = Math.min(Math.max(Math.round(lighting[i + 1]), 0x00), 0xFF);
            }
        }
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

        final FrameLayout flMul = dialog.findViewById(R.id.mul);
        final FrameLayout flAdd = dialog.findViewById(R.id.add);
        final View vMul = flMul.findViewById(R.id.v_color);
        final View vAdd = flAdd.findViewById(R.id.v_color);

        vMul.setBackgroundColor(Color.argb(lighting[6], lighting[0], lighting[2], lighting[4]));
        vAdd.setBackgroundColor(Color.argb((int) lighting[7], (int) lighting[1], (int) lighting[3], (int) lighting[5]));

        flMul.setOnClickListener(v -> RgbColorPicker.make(context, R.string.slope, (oldColor, newColor) -> {
            lighting[0] = Color.red(newColor);
            lighting[2] = Color.green(newColor);
            lighting[4] = Color.blue(newColor);
            lighting[6] = Color.alpha(newColor);
            vMul.setBackgroundColor(Color.toArgb(newColor));
            onLightingChangeListener.onChanged(lighting, true);
        }, Color.pack(lighting[0], lighting[2], lighting[4], lighting[6])).show());

        flAdd.setOnClickListener(v -> RgbColorPicker.make(context, R.string.offset, (oldColor, newColor) -> {
            @ColorInt final int newColorInt = Color.toArgb(newColor);
            lighting[1] = Color.red(newColorInt);
            lighting[3] = Color.green(newColorInt);
            lighting[5] = Color.blue(newColorInt);
            lighting[7] = Color.alpha(newColorInt);
            vAdd.setBackgroundColor(Color.toArgb(newColor));
            onLightingChangeListener.onChanged(lighting, true);
        }, Color.pack(Color.argb((int) lighting[7], (int) lighting[1], (int) lighting[3], (int) lighting[5]))).show());
    }
}
