package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

import javax.microedition.khronos.opengles.GL;

public class ColorRangeDialog {

    public interface OnColorRangeChangeListener {
        void onChange(int range);
    }

    private final AlertDialog.Builder builder;
    private OnColorRangeChangeListener listener;

    private int range = 0b111111;

    public ColorRangeDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .setTitle(R.string.color_range)
                .setView(R.layout.color_range);
    }

    public ColorRangeDialog setDefaultRange(int range) {
        this.range = range;
        return this;
    }

    public ColorRangeDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        return this;
    }

    public ColorRangeDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public ColorRangeDialog setOnColorRangeChangeListener(OnColorRangeChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public void show() {

        AlertDialog dialog = builder.show();

        android.view.Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        GridLayout gl = dialog.findViewById(R.id.gl);

        for (int i = 0; i < 6; ++i) {
            CheckBox cb = (CheckBox) gl.getChildAt(i + 3);
            final int inv = 5 - i;
            cb.setChecked((range >> inv & 0b1) == 0b1);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                range = isChecked ? range | 0b1 << inv : range ^ 0b1 << inv ;
                listener.onChange(range);
            });
        }
    }
}
