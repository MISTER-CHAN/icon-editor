package com.misterchan.iconeditor;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

public abstract class ColorPicker {
    protected static final int EDITOR_TYPE_NUM_DEC = EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL;
    protected static final int EDITOR_TYPE_NUM_DEC_SIGNED = EDITOR_TYPE_NUM_DEC | EditorInfo.TYPE_NUMBER_FLAG_SIGNED;

    public interface OnColorPickListener {
        void onPick(Long oldColor, Long newColor);
    }

    // Positions of first view and last view in alpha row
    private static final int GL_POS_ALPHA_BEGIN = 0, GL_POS_ALPHA_END = 3;

    // Positions of first view and last view in extra row
    private static final int GL_POS_EXTRA_BEGIN = 12, GL_POS_EXTRA_END = 15;

    // Positions of component unit views in GridLayout
    private static final int[] GL_POS_UNIT = new int[]{4, 7, 10};

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

    protected static void showUnits(GridLayout gl) {
        for (final int i : GL_POS_UNIT) {
            gl.getChildAt(i).setVisibility(View.VISIBLE);
        }
    }

    public abstract void show();
}
