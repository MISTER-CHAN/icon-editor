package com.misterchan.iconeditor;

import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.GridLayout;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

abstract class ColorPicker {
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

    protected static void hideOtherColorPickers(AlertDialog dialog) {
        dialog.findViewById(R.id.l_other_color_pickers).setVisibility(View.GONE);
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
