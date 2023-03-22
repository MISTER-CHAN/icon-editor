package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.widget.SeekBar;

import androidx.annotation.ColorLong;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.dialog.ColorPicker;

public abstract class ArgbColorPicker extends ColorPicker {

    protected final Context context;
    protected SeekBar sbAlpha;
    protected SeekBar sbRed, sbGreen, sbBlue;
    protected TextInputEditText tietAlpha;
    protected TextInputEditText tietRed, tietGreen, tietBlue;

    protected void initViews(AlertDialog dialog) {
        sbAlpha = dialog.findViewById(R.id.sb_alpha);
        sbRed = dialog.findViewById(R.id.sb_comp_0);
        sbGreen = dialog.findViewById(R.id.sb_comp_1);
        sbBlue = dialog.findViewById(R.id.sb_comp_2);
        tietAlpha = dialog.findViewById(R.id.tiet_alpha);
        tietRed = dialog.findViewById(R.id.tiet_comp_0);
        tietGreen = dialog.findViewById(R.id.tiet_comp_1);
        tietBlue = dialog.findViewById(R.id.tiet_comp_2);
        vPreview = dialog.findViewById(R.id.v_color);
    }

    protected ArgbColorPicker(Context context, int titleId,
                              final OnColorPickListener onColorPickListener,
                              @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        this.context = context;
        dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(titleId)
                .setView(R.layout.color_picker);

        if (oldColor != null) {
            this.oldColor = oldColor;
            if (neutralFunction != 0) {
                dialogBuilder.setNeutralButton(neutralFunction,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, null));
            }
        } else {
            this.oldColor = Color.BLACK;
        }

        make();
    }

    protected void make() {
    }

    public static ColorPicker make(Context context, int titleId,
                                   final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return make(context, titleId, onColorPickListener, oldColor, 0);
    }

    public static ColorPicker make(Context context, int titleId,
                                   final OnColorPickListener onColorPickListener,
                                   @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        return Settings.INST.argbColorType() ?
                new ArgbColorLongPicker(context, titleId, onColorPickListener, oldColor, neutralFunction) :
                new ArgbColorIntPicker(context, titleId, onColorPickListener, oldColor, neutralFunction);
    }

    protected void showOtherColorPickers(AlertDialog dialog, OnColorPickListener l) {
        dialog.findViewById(R.id.tv_cmyk).setOnClickListener(v ->
                CmykColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.tv_hsv).setOnClickListener(v ->
                HsvColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.tv_lab).setOnClickListener(v ->
                LabColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.tv_xyz).setOnClickListener(v ->
                XyzColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.tv_xyy).setOnClickListener(v ->
                XyYColorPicker.make(context, l, newColor).show());
    }
}
