package com.misterchan.iconeditor.dialog;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.misterchan.iconeditor.R;

public abstract class FilterDialog {
    final AlertDialog.Builder builder;

    FilterDialog(Context context) {
        builder = new MaterialAlertDialogBuilder(context);
    }

    abstract void onFilterCommit();

    public FilterDialog setOnActionListener(Runnable onPBClickListener, Runnable onCancelListener) {
        return setOnActionListener(onPBClickListener, onCancelListener, true);
    }

    public FilterDialog setOnActionListener(Runnable onPBClickListener,
                                            Runnable onCancelListener, boolean showCancelButton) {
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            onPBClickListener.run();
            onFilterCommit();
        });

        builder.setOnCancelListener(dialog -> onCancelListener.run());
        if (showCancelButton) {
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> onCancelListener.run());
        }

        return this;
    }

    public FilterDialog setOnActionListener(Runnable onDismissListener) {
        builder.setOnDismissListener(dialog -> onDismissListener.run());
        builder.setPositiveButton(R.string.ok, null);
        return this;
    }

    public FilterDialog setOkButton() {
        builder.setPositiveButton(R.string.ok, null);
        return this;
    }

    public abstract void show();
}
