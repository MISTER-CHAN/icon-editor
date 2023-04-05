package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.misterchan.iconeditor.listener.OnSBChangeListener;
import com.misterchan.iconeditor.R;

public class ColorBalanceDialog {

    private final AlertDialog.Builder builder;
    private LightingDialog.OnLightingChangedListener listener;
    private SeekBar sbRed, sbGreen, sbBlue;

    @Size(8)
    private final float[] lighting = new float[]{
            1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f
    };

    private final OnSBChangeListener onProgressChangeListener = (progress, stopped) -> {
        final float r = sbRed.getProgress() / 10.0f, g = sbGreen.getProgress() / 10.0f, b = sbBlue.getProgress() / 10.0f;
        final float average = (r + g + b) / 3.0f;
        lighting[0] = 1.0f + r - average;
        lighting[2] = 1.0f + g - average;
        lighting[4] = 1.0f + b - average;
        listener.onChanged(lighting, stopped);
    };

    public ColorBalanceDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.color_balance)
                .setView(R.layout.color_balance);
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

        sbRed = dialog.findViewById(R.id.sb_red);
        sbGreen = dialog.findViewById(R.id.sb_green);
        sbBlue = dialog.findViewById(R.id.sb_blue);

        sbRed.setOnSeekBarChangeListener(onProgressChangeListener);
        sbGreen.setOnSeekBarChangeListener(onProgressChangeListener);
        sbBlue.setOnSeekBarChangeListener(onProgressChangeListener);
    }
}
