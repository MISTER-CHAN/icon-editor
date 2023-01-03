package com.misterchan.iconeditor;

import android.content.Context;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

public class ArgbColorPicker extends ColorPicker {

    private Context context;
    private EditText etAlpha;
    private EditText etRed, etGreen, etBlue;
    private int radix = 16;
    private SeekBar sbAlpha;
    private SeekBar sbRed, sbGreen, sbBlue;
    private String format;

    private void loadColor(@ColorInt int color) {
        etRed.setText(String.format(format, Color.red(color)));
        etGreen.setText(String.format(format, Color.green(color)));
        etBlue.setText(String.format(format, Color.blue(color)));
    }

    public static ColorPicker make(Context context, int titleId,
                                   final OnColorPickListener onColorPickListener, @ColorInt final Integer oldColor) {
        return make(context, titleId,
                onColorPickListener, oldColor, 0);
    }

    public static ColorPicker make(Context context, int titleId,
                                   final OnColorPickListener onColorPickListener,
                                   @ColorInt final Integer oldColor, @StringRes int neutralFunction) {
        final Settings settings = ((MainApplication) context.getApplicationContext()).getSettings();
        final ArgbColorPicker picker = new ArgbColorPicker();
        picker.context = context;
        picker.format = settings.getArgbComponentsFormat();
        picker.radix = settings.getArgbComponentsRadix();
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(titleId)
                .setView(R.layout.color_picker);

        if (oldColor != null) {
            picker.oldColor = oldColor;
            if (neutralFunction != 0) {
                picker.dialogBuilder.setNeutralButton(neutralFunction,
                        (dialog, which) -> onColorPickListener.onPick(oldColor, null));
            }
        } else {
            picker.oldColor = Color.BLACK;
        }

        return picker;
    }

    private void onComponentChanged(String s, SeekBar seekBar) {
        try {
            seekBar.setProgress(Integer.parseUnsignedInt(s, radix));
        } catch (NumberFormatException e) {
        }
        newColor = Color.argb(
                sbAlpha.getProgress(),
                sbRed.getProgress(),
                sbGreen.getProgress(),
                sbBlue.getProgress());
        vPreview.setBackgroundColor(newColor);
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        etAlpha = dialog.findViewById(R.id.et_alpha);
        etRed = dialog.findViewById(R.id.et_red);
        etGreen = dialog.findViewById(R.id.et_green);
        etBlue = dialog.findViewById(R.id.et_blue);
        sbAlpha = dialog.findViewById(R.id.sb_alpha);
        sbRed = dialog.findViewById(R.id.sb_red);
        sbGreen = dialog.findViewById(R.id.sb_green);
        sbBlue = dialog.findViewById(R.id.sb_blue);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        sbAlpha.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etAlpha.setText(String.format(format, progress)));
        sbRed.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etRed.setText(String.format(format, progress)));
        sbGreen.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etGreen.setText(String.format(format, progress)));
        sbBlue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etBlue.setText(String.format(format, progress)));
        etAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbAlpha));
        etRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbRed));
        etGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbGreen));
        etBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbBlue));

        etAlpha.setText(String.format(format, Color.alpha(oldColor)));
        loadColor(oldColor);

        final OnColorPickListener onColorPickListener = (oldColor, newColor) -> loadColor(newColor);

        dialog.findViewById(R.id.tv_cmyk).setOnClickListener(v ->
                CmykColorPicker
                        .make(context, onColorPickListener, newColor)
                        .show());

        dialog.findViewById(R.id.tv_hsv).setOnClickListener(v ->
                HSVColorPicker
                        .make(context, onColorPickListener, newColor)
                        .show());

        dialog.findViewById(R.id.tv_lab).setOnClickListener(v ->
                LabColorPicker
                        .make(context, onColorPickListener, newColor)
                        .show());

        dialog.findViewById(R.id.tv_xyz).setOnClickListener(v ->
                XyzColorPicker
                        .make(context, onColorPickListener, newColor)
                        .show());
    }
}
