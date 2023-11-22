package com.misterchan.iconeditor.colorpicker;

import android.graphics.ColorSpace;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.IntRange;

import com.misterchan.iconeditor.Settings;

public abstract class ColorPicker {
    public static final ColorSpace SRGB = ColorSpace.get(ColorSpace.Named.SRGB);
    public static final int EDITOR_TYPE_NUM = EditorInfo.TYPE_CLASS_NUMBER;
    public static final int EDITOR_TYPE_NUM_DEC = EDITOR_TYPE_NUM | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL;
    public static final int EDITOR_TYPE_NUM_DEC_SIGNED = EDITOR_TYPE_NUM_DEC | EditorInfo.TYPE_NUMBER_FLAG_SIGNED;

    /**
     * @param colorRep  <code>false</code>: Color int, <code>true</code>: Color long
     * @param compCount <code>false</code>: 3, <code>true</code>: 4
     */
    public record Properties(boolean colorRep, boolean compCount,
                             CharSequence c0, CharSequence c1, CharSequence c2,
                             CharSequence c0Suffix, CharSequence c1Suffix, CharSequence c2Suffix,
                             float c0Min, float c0Max,
                             float c1Min, float c1Max,
                             float c2Min, float c2Max,
                             boolean c0Circular,
                             int c0InputType, int c1InputType, int c2InputType,
                             boolean compBase10) {

        public Properties(CharSequence c0, CharSequence c1, CharSequence c2,
                          float c0Min, float c0Max, float c1Min, float c1Max, float c2Min, float c2Max,
                          int c0InputType, int c1InputType, int c2InputType) {
            this(true, c0, c1, c2, c0Min, c0Max, c1Min, c1Max, c2Min, c2Max,
                    c0InputType, c1InputType, c2InputType, true);
        }

        public Properties(boolean colorRep, CharSequence c0, CharSequence c1, CharSequence c2,
                          float c0Min, float c0Max, float c1Min, float c1Max, float c2Min, float c2Max,
                          int c0InputType, int c1InputType, int c2InputType, boolean compBase10) {
            this(colorRep, false, c0, c1, c2,
                    null, null, null,
                    c0Min, c0Max, c1Min, c1Max, c2Min, c2Max, false,
                    c0InputType, c1InputType, c2InputType, compBase10);
        }

        public Properties(CharSequence c0, CharSequence c1, CharSequence c2, boolean c0Circular) {
            this(true, false, c0, c1, c2,
                    "°", "%", "%",
                    0.0f, 360.0f, 0.0f, 100.0f, 0.0f, 100.0f, c0Circular,
                    EDITOR_TYPE_NUM_DEC, EDITOR_TYPE_NUM_DEC, EDITOR_TYPE_NUM_DEC, true);
        }

        public Properties(boolean compCount, CharSequence c0, CharSequence c1, CharSequence c2) {
            this(false, compCount, c0, c1, c2, null, null, null,
                    0x00, 0xFF, 0x00, 0xFF, 0x00, 0xFF, false,
                    EDITOR_TYPE_NUM, EDITOR_TYPE_NUM, EDITOR_TYPE_NUM,
                    Settings.INST.colorIntCompRadix() == 10);
        }
    }

    Properties prop;

    public abstract long color();

    public abstract int colorInt();

    public void dismiss() {
    }

    public abstract float getComponent(@IntRange(from = 0, to = 3) int index);

    public Properties prop() {
        return prop;
    }

    public abstract void setAlpha(float a);

    public abstract void setComponent(@IntRange(from = 0, to = 3) int index, float c);
}
