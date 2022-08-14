package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

public class ColorPicker {

    public interface OnColorPickListener {
        void onPick(Integer oldColor, Integer newColor);
    }

    private static final String FORMAT_02X = "%02X";

    private EditText etAlpha;
    private EditText etBlue;
    private EditText etGreen;
    private EditText etRed;
    private int newColor;
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

    public void show(Context context, final OnColorPickListener onColorPickListener) {
        show(context, onColorPickListener, null, false);
    }

    public void show(Context context, final OnColorPickListener onColorPickListener, final Integer oldColor) {
        show(context, onColorPickListener, oldColor, false);
    }

    public void show(Context context, final OnColorPickListener onColorPickListener, final Integer oldColor, boolean canDeleteOld) {

        AlertDialog.Builder colorPickingDialogBuilder = new AlertDialog.Builder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(R.string.color_picker)
                .setView(R.layout.color_picker);

        int color;
        if (oldColor != null) {
            color = oldColor;
            if (canDeleteOld) {
                colorPickingDialogBuilder.setNeutralButton(R.string.delete, (dialog, which) -> onColorPickListener.onPick(oldColor, null));
            }
        } else {
            color = Color.BLACK;
        }

        AlertDialog colorPickingDialog = colorPickingDialogBuilder.show();
        etAlpha = colorPickingDialog.findViewById(R.id.et_alpha);
        etBlue = colorPickingDialog.findViewById(R.id.et_blue);
        etGreen = colorPickingDialog.findViewById(R.id.et_green);
        etRed = colorPickingDialog.findViewById(R.id.et_red);
        sbAlpha = colorPickingDialog.findViewById(R.id.sb_alpha);
        sbBlue = colorPickingDialog.findViewById(R.id.sb_blue);
        sbGreen = colorPickingDialog.findViewById(R.id.sb_green);
        sbRed = colorPickingDialog.findViewById(R.id.sb_red);
        vPreview = colorPickingDialog.findViewById(R.id.v_color_preview);

        sbAlpha.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etAlpha.setText(String.format(FORMAT_02X, progress)));
        sbBlue.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etBlue.setText(String.format(FORMAT_02X, progress)));
        sbGreen.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etGreen.setText(String.format(FORMAT_02X, progress)));
        sbRed.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etRed.setText(String.format(FORMAT_02X, progress)));
        etAlpha.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbAlpha));
        etBlue.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbBlue));
        etGreen.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbGreen));
        etRed.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbRed));

        etAlpha.setText(String.format(FORMAT_02X, Color.alpha(color)));
        etRed.setText(String.format(FORMAT_02X, Color.red(color)));
        etGreen.setText(String.format(FORMAT_02X, Color.green(color)));
        etBlue.setText(String.format(FORMAT_02X, Color.blue(color)));
    }
}
