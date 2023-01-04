package com.misterchan.iconeditor;

import android.content.Context;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.ColorLong;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

public abstract class ArgbColorPicker extends ColorPicker {

    protected Context context;
    protected EditText etAlpha;
    protected EditText etRed, etGreen, etBlue;
    protected SeekBar sbAlpha;
    protected SeekBar sbRed, sbGreen, sbBlue;

    protected void initViews(AlertDialog dialog) {
        etAlpha = dialog.findViewById(R.id.et_alpha);
        etRed = dialog.findViewById(R.id.et_red);
        etGreen = dialog.findViewById(R.id.et_green);
        etBlue = dialog.findViewById(R.id.et_blue);
        sbAlpha = dialog.findViewById(R.id.sb_alpha);
        sbRed = dialog.findViewById(R.id.sb_red);
        sbGreen = dialog.findViewById(R.id.sb_green);
        sbBlue = dialog.findViewById(R.id.sb_blue);
        vPreview = dialog.findViewById(R.id.v_color_preview);
    }

    public static ColorPicker make(Context context, int titleId,
                                   final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return make(context, titleId,
                onColorPickListener, oldColor, 0);
    }

    public static ColorPicker make(Context context, int titleId,
                                   final OnColorPickListener onColorPickListener,
                                   @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        final Settings settings = ((MainApplication) context.getApplicationContext()).getSettings();
        final boolean act = settings.getArgbComponentType();
        final ArgbColorPicker picker = act ? new ArgbColorIntPicker() : new ArgbColorLongPicker();
        picker.context = context;
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(titleId)
                .setView(act ? R.layout.color_int_picker : R.layout.color_long_picker);

        if (oldColor != null) {
            picker.oldColor = oldColor;
            if (neutralFunction != 0) {
                picker.dialogBuilder.setNeutralButton(neutralFunction,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, null));
            }
        } else {
            picker.oldColor = Color.BLACK;
        }

        picker.make(settings);

        return picker;
    }

    protected void make(Settings settings) {
    }

    protected void showOtherColorPickers(AlertDialog dialog, OnColorPickListener l) {
        dialog.findViewById(R.id.tv_cmyk).setOnClickListener(v ->
                CmykColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.tv_hsv).setOnClickListener(v ->
                HSVColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.tv_lab).setOnClickListener(v ->
                LabColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.tv_xyz).setOnClickListener(v ->
                XyzColorPicker.make(context, l, newColor).show());

        dialog.findViewById(R.id.tv_xyy).setOnClickListener(v ->
                XyYColorPicker.make(context, l, newColor).show());
    }
}
