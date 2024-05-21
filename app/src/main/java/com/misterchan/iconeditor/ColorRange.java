package com.misterchan.iconeditor;

import androidx.annotation.FloatRange;
import androidx.annotation.Size;

import java.util.Arrays;

public class ColorRange {
    public boolean enabled = false;

    @Size(6)
    public float[] cuboid; // HSV

    @FloatRange(from = 0.0f, to = 1.0f)
    public float transition;

    public ColorRange() {
        cuboid = new float[]{0.0f, 0.0f, 0.0f, 360.0f, 1.0f, 1.0f};
        transition = 0.0f;
    }

    public void set(ColorRange src) {
        if (!enabled && !src.enabled) {
            return;
        }
        enabled = src.enabled;
        transition = src.transition;
        cuboid = Arrays.copyOf(src.cuboid, 6);
    }

    public void update() {
        enabled = cuboid[0] != 0.0f || cuboid[1] != 0.0f || cuboid[2] != 0.0f
                || cuboid[3] != 360.0f || cuboid[4] != 1.0f || cuboid[5] != 1.0f;
    }
}
