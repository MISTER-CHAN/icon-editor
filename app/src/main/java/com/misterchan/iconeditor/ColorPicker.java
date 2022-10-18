package com.misterchan.iconeditor;

import android.content.Context;
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

    private AlertDialog.Builder dialogBuilder;
    private Context context;
    private EditText etAlpha;
    private EditText etBlue;
    private EditText etGreen;
    private EditText etRed;
    private int radix = 16;
    private SeekBar sbAlpha;
    private SeekBar sbBlue;
    private SeekBar sbGreen;
    private SeekBar sbRed;
    private String format = FORMAT_02X;
    private View vPreview;

    @ColorInt
    private int newColor, oldColor;

    private void loadColor(@ColorInt int color) {
        etRed.setText(String.format(format, Color.red(color)));
        etGreen.setText(String.format(format, Color.green(color)));
        etBlue.setText(String.format(format, Color.blue(color)));
    }

    public static ColorPicker make(Context context, int titleId, Settings settings, final OnColorPickListener onColorPickListener) {
        return make(context, titleId, settings, onColorPickListener, null, false);
    }

    public static ColorPicker make(Context context, int titleId, Settings settings, final OnColorPickListener onColorPickListener, @ColorInt final Integer oldColor) {
        return make(context, titleId, settings, onColorPickListener, oldColor, false);
    }

    public static ColorPicker make(Context context, int titleId, Settings settings, final OnColorPickListener onColorPickListener, @ColorInt final Integer oldColor, boolean canDeleteOld) {
        final ColorPicker picker = new ColorPicker();
        picker.context = context;
        picker.format = settings.getArgbChannelsFormat();
        picker.radix = settings.getArgbChannelsRadix();
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

    private void onChannelChanged(String s, SeekBar seekBar) {
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

    public void show() {

        final AlertDialog dialog = dialogBuilder.show();

        etAlpha = dialog.findViewById(R.id.et_alpha);
        etBlue = dialog.findViewById(R.id.et_blue);
        etGreen = dialog.findViewById(R.id.et_green);
        etRed = dialog.findViewById(R.id.et_red);
        sbAlpha = dialog.findViewById(R.id.sb_alpha);
        sbBlue = dialog.findViewById(R.id.sb_blue);
        sbGreen = dialog.findViewById(R.id.sb_green);
        sbRed = dialog.findViewById(R.id.sb_red);
        vPreview = dialog.findViewById(R.id.v_color_preview);

        sbAlpha.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etAlpha.setText(String.format(format, progress)));
        sbBlue.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etBlue.setText(String.format(format, progress)));
        sbGreen.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etGreen.setText(String.format(format, progress)));
        sbRed.setOnSeekBarChangeListener((OnSeekBarProgressChangeListener) (seekBar, progress) -> etRed.setText(String.format(format, progress)));
        etAlpha.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbAlpha));
        etBlue.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbBlue));
        etGreen.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbGreen));
        etRed.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbRed));
        etAlpha.setText(String.format(format, Color.alpha(oldColor)));
        loadColor(oldColor);

        dialog.findViewById(R.id.tv_hsv).setOnClickListener(v ->
                HSVColorPicker
                        .make(context,
                                ((oldColor, newColor) -> loadColor(newColor)),
                                this.newColor)
                        .show());
    }
}
