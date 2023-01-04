package com.misterchan.iconeditor;

import android.view.View;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

public abstract class ColorPicker {

    public interface OnColorPickListener {
        void onPick(Long oldColor, Long newColor);
    }

    protected AlertDialog.Builder dialogBuilder;
    protected View vPreview;

    @ColorInt
    protected long newColor, oldColor;

    public abstract void show();
}
