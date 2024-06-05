package com.misterchan.iconeditor.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Shader;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.misterchan.iconeditor.R;

public class FillWithClipDialog extends FilterDialog {
    public interface OnChangeListener {
        void onChange(Shader.TileMode tileMode);
    }

    private final OnChangeListener listener;
    private Shader.TileMode tileMode = Shader.TileMode.REPEAT;

    @SuppressLint("NonConstantResourceId")
    private final MaterialButtonToggleGroup.OnButtonCheckedListener onTileModeButtonCheckedListener = (group, checkedId, isChecked) -> {
        if (!isChecked) {
            return;
        }
        tileMode = switch (checkedId) {
            case R.id.b_repeat -> Shader.TileMode.REPEAT;
            case R.id.b_mirror -> Shader.TileMode.MIRROR;
            default -> Shader.TileMode.REPEAT;
        };
        onChange();
    };

    public FillWithClipDialog(Context context, OnChangeListener listener) {
        super(context);
        builder.setTitle(R.string.fill_with_clip)
                .setView(R.layout.fill_with_clip);

        this.listener = listener;
    }

    private void onChange() {
        listener.onChange(tileMode);
    }

    @Override
    void onFilterCommit() {
        listener.onChange(tileMode);
    }

    public FillWithClipDialog setOnDismissListener(DialogInterface.OnDismissListener listener) {
        builder.setOnDismissListener(listener);
        return this;
    }

    @Override
    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final MaterialButtonToggleGroup btgTileMode = dialog.findViewById(R.id.btg_tile_modes);

        btgTileMode.addOnButtonCheckedListener(onTileModeButtonCheckedListener);
    }
}
