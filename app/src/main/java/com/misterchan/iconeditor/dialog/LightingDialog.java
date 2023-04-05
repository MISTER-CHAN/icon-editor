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

public class LightingDialog {

    public interface OnLightingChangedListener {
        void onChanged(@Size(8) float[] lighting, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private OnLightingChangedListener onLightingChangeListener;

    @Size(8)
    private final float[] lighting = new float[]{1.0f, 0.0f, 1.0f, 0.0f, 1.0f ,0.0f, 1.0f ,0.0f};

    public LightingDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.channel_lighting)
                .setView(R.layout.channel_lighting);
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

        ((SeekBar) dialog.findViewById(R.id.sb_red_scale)).setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> setElement(0, progress / 10.0f, stopped));
        ((SeekBar) dialog.findViewById(R.id.sb_red_shift)).setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> setElement(1, progress, stopped));
        ((SeekBar) dialog.findViewById(R.id.sb_green_scale)).setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> setElement(2, progress / 10.0f, stopped));
        ((SeekBar) dialog.findViewById(R.id.sb_green_shift)).setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> setElement(3, progress, stopped));
        ((SeekBar) dialog.findViewById(R.id.sb_blue_scale)).setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> setElement(4, progress / 10.0f, stopped));
        ((SeekBar) dialog.findViewById(R.id.sb_blue_shift)).setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> setElement(5, progress, stopped));
        ((SeekBar) dialog.findViewById(R.id.sb_alpha_scale)).setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> setElement(6, progress / 10.0f, stopped));
        ((SeekBar) dialog.findViewById(R.id.sb_alpha_shift)).setOnSeekBarChangeListener((OnSBChangeListener) (progress, stopped) -> setElement(7, progress, stopped));
    }
}
