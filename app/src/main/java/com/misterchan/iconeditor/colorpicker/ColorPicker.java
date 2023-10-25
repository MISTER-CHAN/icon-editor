package com.misterchan.iconeditor.colorpicker;

import android.annotation.SuppressLint;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.GridLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;

public abstract class ColorPicker {
    protected static final int EDITOR_TYPE_NUM = EditorInfo.TYPE_CLASS_NUMBER;
    protected static final int EDITOR_TYPE_NUM_DEC = EDITOR_TYPE_NUM | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL;
    protected static final int EDITOR_TYPE_NUM_DEC_SIGNED = EDITOR_TYPE_NUM_DEC | EditorInfo.TYPE_NUMBER_FLAG_SIGNED;
    protected static final KeyListener KEY_LISTENER_HEX = DigitsKeyListener.getInstance("0123456789ABCDEFabcdef");

    // Positions of first view and last view in alpha row
    private static final int GL_POS_ALPHA_BEGIN = 0, GL_POS_ALPHA_END = 2;

    // Positions of first view and last view in extra row
    private static final int GL_POS_EXTRA_BEGIN = 8, GL_POS_EXTRA_END = 10;

    public interface OnColorPickListener {
        void onPick(Long oldColor, Long newColor);
    }

    protected AlertDialog.Builder dialogBuilder;
    protected View vPreview;

    @ColorInt
    protected long newColor, oldColor;

    @SuppressLint("NonConstantResourceId")
    protected void deployNeutralFunction(final OnColorPickListener onColorPickListener,
                                         @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        if (oldColor != null) {
            this.oldColor = oldColor;
            if (neutralFunction != 0) {
                dialogBuilder.setNeutralButton(neutralFunction, (dialog, which) -> {
                    switch (neutralFunction) {
                        case R.string.swap -> onColorPickListener.onPick(null, newColor);
                        case R.string.delete -> onColorPickListener.onPick(oldColor, null);
                    }
                });
            }
        } else {
            this.oldColor = Color.BLACK;
        }
    }

    protected static void showExtraComp(GridLayout gl) {
        setChildVisibilities(gl, GL_POS_EXTRA_BEGIN, GL_POS_EXTRA_END, View.VISIBLE);
    }

    protected static void hideAlphaComp(GridLayout gl) {
        setChildVisibilities(gl, GL_POS_ALPHA_BEGIN, GL_POS_ALPHA_END, View.GONE);
    }

    protected static void setChildVisibilities(GridLayout gl, int begin, int end, int visibility) {
        for (int i = begin; i < end; ++i) {
            gl.getChildAt(i).setVisibility(visibility);
        }
    }

    public abstract void show();
}
