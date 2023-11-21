package com.misterchan.iconeditor.colorpicker;

import android.graphics.ColorSpace;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.IntRange;

public abstract class ColorPicker {
    static final ColorSpace SRGB = ColorSpace.get(ColorSpace.Named.SRGB);
    static final int EDITOR_TYPE_NUM = EditorInfo.TYPE_CLASS_NUMBER;
    static final int EDITOR_TYPE_NUM_DEC = EDITOR_TYPE_NUM | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL;
    static final int EDITOR_TYPE_NUM_DEC_SIGNED = EDITOR_TYPE_NUM_DEC | EditorInfo.TYPE_NUMBER_FLAG_SIGNED;
    static final KeyListener KEY_LISTENER_HEX = DigitsKeyListener.getInstance("0123456789ABCDEFabcdef");

    /**
     * @param colorRep  <code>false</code>: Color int, <code>true</code>: Color long
     * @param compCount <code>false</code>: 3, <code>true</code>: 4
     */
    record Properties(boolean colorRep, boolean compCount, CharSequence c0, CharSequence c1,
                      CharSequence c2,
                      float c0Min, float c0Max, float c1Min, float c1Max, float c2Min, float c2Max,
                      int c0InputType, int c1InputType, int c2InputType, KeyListener keyListener) {
    }

    Properties prop;

    abstract long color();

    abstract int colorInt();

    void dismiss() {
    }

    abstract float getComponent(@IntRange(from = 0, to = 3) int index);

    abstract void setAlpha(float a);

    abstract void setComponent(@IntRange(from = 0, to = 3) int index, float c);
}
