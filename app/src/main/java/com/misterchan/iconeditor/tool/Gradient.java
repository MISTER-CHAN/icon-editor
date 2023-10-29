package com.misterchan.iconeditor.tool;

import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import androidx.annotation.ColorLong;

public class Gradient {
    public enum Colors {
        PAINTS, PALETTE
    }

    public enum Type {
        LINEAR, RADIAL, SWEEP
    }

    public Colors colors = Colors.PAINTS;
    public Type type = Type.LINEAR;

    public Shader createShader(float x0, float y0, float x1, float y1, @ColorLong long... colors) {
        return switch (type) {
            case LINEAR -> new LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP);

            case RADIAL -> new RadialGradient(x0, y0,
                    (float) Math.hypot(Math.abs(x0 - x1), Math.abs(y0 - y1)), colors, null, Shader.TileMode.CLAMP);

            case SWEEP -> new SweepGradient(x0, y0, colors, null);
        };
    }
}
