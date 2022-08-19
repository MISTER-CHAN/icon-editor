package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class MergeAsHiddenDialog {

    public interface OnFinishSettingListener {
        void onFinish(float[] scale);
    }

    private final AlertDialog.Builder builder;
    private SeekBar sbScaleToBlack, sbScaleToWhite;

    public MergeAsHiddenDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.merge_as_hidden)
                .setView(R.layout.merge_as_hidden);
    }

    public MergeAsHiddenDialog setOnFinishSettingListener(OnFinishSettingListener listener) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            listener.onFinish(new float[]{sbScaleToWhite.getProgress() / 8.0f, sbScaleToBlack.getProgress() / 8.0f});
        });
        return this;
    }

    public void show() {
        AlertDialog dialog = builder.show();

        sbScaleToBlack = dialog.findViewById(R.id.sb_scale_to_black);
        sbScaleToWhite = dialog.findViewById(R.id.sb_scale_to_white);
    }
}
