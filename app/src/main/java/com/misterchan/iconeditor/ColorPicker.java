package com.misterchan.iconeditor;

import android.view.View;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

public abstract class ColorPicker {

    public interface OnColorPickListener {
        void onPick(Integer oldColor, Integer newColor);
    }

    protected AlertDialog.Builder dialogBuilder;
    protected View vPreview;

    @ColorInt
    protected int newColor, oldColor;

    public abstract void show();
}
