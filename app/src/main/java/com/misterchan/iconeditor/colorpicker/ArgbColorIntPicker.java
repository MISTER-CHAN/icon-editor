package com.misterchan.iconeditor.colorpicker;

import android.content.Context;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.listener.OnSBProgressChangedListener;
import com.misterchan.iconeditor.Settings;

public class ArgbColorIntPicker extends ArgbColorPicker {

    private int radix = 16;
    private String format;

    protected ArgbColorIntPicker(Context context, int titleId,
                                 final ColorPicker.OnColorPickListener onColorPickListener,
                                 @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        super(context, titleId, onColorPickListener, oldColor, neutralFunction);
    }

    private void loadColor(@ColorInt int color) {
        tietRed.setText(String.format(format, Color.red(color)));
        tietGreen.setText(String.format(format, Color.green(color)));
        tietBlue.setText(String.format(format, Color.blue(color)));
    }

    @Override
    protected void make() {
        format = Settings.INST.argbCompFormat();
        radix = Settings.INST.argbCompRadix();
    }

    protected void onComponentChanged(String s, SeekBar seekBar) {
        try {
            seekBar.setProgress(Integer.parseUnsignedInt(s, radix));
        } catch (NumberFormatException e) {
        }
        final int color = Color.argb(sbAlpha.getProgress(),
                sbRed.getProgress(), sbGreen.getProgress(), sbBlue.getProgress());
        newColor = Color.pack(color);
        vPreview.setBackgroundColor(color);
    }

    @Override
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        initViews(dialog);

        if (radix <= 10) {
            tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM);
            tietRed.setInputType(ColorPicker.EDITOR_TYPE_NUM);
            tietGreen.setInputType(ColorPicker.EDITOR_TYPE_NUM);
            tietBlue.setInputType(ColorPicker.EDITOR_TYPE_NUM);
        } else if (radix == 16) {
            tietAlpha.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
            tietRed.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
            tietGreen.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
            tietBlue.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
        }

        sbAlpha.setOnSeekBarChangeListener((OnSBProgressChangedListener) (seekBar, progress) -> tietAlpha.setText(String.format(format, progress)));
        sbRed.setOnSeekBarChangeListener((OnSBProgressChangedListener) (seekBar, progress) -> tietRed.setText(String.format(format, progress)));
        sbGreen.setOnSeekBarChangeListener((OnSBProgressChangedListener) (seekBar, progress) -> tietGreen.setText(String.format(format, progress)));
        sbBlue.setOnSeekBarChangeListener((OnSBProgressChangedListener) (seekBar, progress) -> tietBlue.setText(String.format(format, progress)));
        tietAlpha.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbAlpha));
        tietRed.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbRed));
        tietGreen.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbGreen));
        tietBlue.addTextChangedListener((AfterTextChangedListener) s -> onComponentChanged(s, sbBlue));

        final int color = Color.toArgb(oldColor);
        tietAlpha.setText(String.format(format, Color.alpha(color)));
        loadColor(color);

        final ColorPicker.OnColorPickListener onColorPickListener = (oldColor, newColor) -> loadColor(Color.toArgb(newColor));
        showOtherColorPickers(dialog, onColorPickListener);
    }
}
