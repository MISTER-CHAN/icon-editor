package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class LevelsDialog {

    public interface OnLevelsChangeListener {
        void onChange(int shadows, int highlights);
    }

    private final AlertDialog.Builder builder;
    private OnLevelsChangeListener listener;

    public LevelsDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.levels)
                .setView(R.layout.levels);
    }

    public LevelsDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public LevelsDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public LevelsDialog setOnLevelsChangeListener(OnLevelsChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public void show() {
        AlertDialog dialog = builder.show();

        android.view.Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final SeekBar sbHighlights = dialog.findViewById(R.id.sb_highlights);
        final SeekBar sbShadows = dialog.findViewById(R.id.sb_shadows);
        sbHighlights.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> listener.onChange(sbShadows.getProgress(), sbHighlights.getProgress()));
        sbShadows.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> listener.onChange(sbShadows.getProgress(), sbHighlights.getProgress()));
    }
}
