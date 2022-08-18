package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

public class ColorPicker {

    public interface OnColorPickListener {
        void onPick(Integer oldColor, Integer newColor);
    }

    private static final String FORMAT_02X = "%02X";

    @ColorInt
    private int newColor, oldColor;

    private AlertDialog.Builder dialogBuilder;
    private EditText etAlpha;
    private EditText etBlue;
    private EditText etGreen;
    private EditText etRed;
    private SeekBar sbAlpha;
    private SeekBar sbBlue;
    private SeekBar sbGreen;
    private SeekBar sbRed;
    private View vPreview;

    private void onChannelChanged(String hex, SeekBar seekBar) {
        try {
            seekBar.setProgress(Integer.parseUnsignedInt(hex, 16));
        } catch (NumberFormatException e) {
        }
        newColor = Color.argb(
                sbAlpha.getProgress(),
                sbRed.getProgress(),
                sbGreen.getProgress(),
                sbBlue.getProgress());
        vPreview.setBackgroundColor(newColor);
    }

    public static ColorPicker make(Context context, int titleId, final OnColorPickListener onColorPickListener) {
        return make(context, titleId, onColorPickListener, null, false);
    }

    public static ColorPicker make(Context context, int titleId, final OnColorPickListener onColorPickListener, @ColorInt final Integer oldColor) {
        return make(context, titleId, onColorPickListener, oldColor, false);
    }

    public static ColorPicker make(Context context, int titleId, final OnColorPickListener onColorPickListener, @ColorInt final Integer oldColor, boolean canDeleteOld) {
        ColorPicker picker = new ColorPicker();
        picker.dialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, picker.newColor))
                .setTitle(titleId)
                .setView(R.layout.color_picker);

        if (oldColor != null) {
            picker.oldColor = oldColor;
            if (canDeleteOld) {
                picker.dialogBuilder.setNeutralButton(R.string.delete, (dialog, which) -> onColorPickListener.onPick(oldColor, null));
            }
        } else {
            picker.oldColor = Color.BLACK;
        }

        return picker;
    }

    public void show() {

        AlertDialog dialog = dialogBuilder.show();

        etAlpha = dialog.findViewById(R.id.et_alpha);
        etBlue = dialog.findViewById(R.id.et_blue);
        etGreen = dialog.findViewById(R.id.et_green);
        etRed = dialog.findViewById(R.id.et_red);
        sbAlpha = dialog.findViewById(R.id.sb_alpha);
        sbBlue = dialog.findViewById(R.id.sb_blue);
        sbGreen = dialog.findViewById(R.id.sb_green);
        sbRed = dialog.findViewById(R.id.sb_red);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        sbAlpha.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etAlpha.setText(String.format(FORMAT_02X, progress)));
        sbBlue.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etBlue.setText(String.format(FORMAT_02X, progress)));
        sbGreen.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etGreen.setText(String.format(FORMAT_02X, progress)));
        sbRed.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etRed.setText(String.format(FORMAT_02X, progress)));
        etAlpha.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbAlpha));
        etBlue.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbBlue));
        etGreen.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbGreen));
        etRed.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbRed));

        etAlpha.setText(String.format(FORMAT_02X, Color.alpha(oldColor)));
        etRed.setText(String.format(FORMAT_02X, Color.red(oldColor)));
        etGreen.setText(String.format(FORMAT_02X, Color.green(oldColor)));
        etBlue.setText(String.format(FORMAT_02X, Color.blue(oldColor)));
    }
}
