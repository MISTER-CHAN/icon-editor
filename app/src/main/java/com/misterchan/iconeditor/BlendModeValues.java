package com.misterchan.iconeditor;

import android.graphics.BlendMode;

class BlendModeValues {
    private static final BlendMode[] values = BlendMode.values();

    public static final int COUNT = values.length;

    public static BlendMode valAt(int index) {
        return values[index];
    }
}
