package com.misterchan.iconeditor;

import android.graphics.Canvas;
import android.graphics.Paint;

public class CellGrid {
    public boolean enabled = false;

    public int sizeX = 16, sizeY = 16;

    public int spacingX = 0, spacingY = 0;

    public int offsetX = 0, offsetY = 0;

    public void draw(Canvas canvas, float l, float t, float r, float b, float tx, float ty, float s, Paint paint) {
        if (sizeX + spacingX > 1) {
            final float scaledSizeX = sizeX * s, scaledSpacingX = spacingX * s;
            float x = (tx >= 0.0f ? tx : tx % (scaledSizeX + scaledSpacingX)) + offsetX % (sizeX + spacingX) * s;
            while (true) {
                if (x >= l) canvas.drawLine(x, t, x, b, paint);
                if ((x += scaledSizeX) > r) break;
                if (spacingX != 0) {
                    if (x >= l) canvas.drawLine(x, t, x, b, paint);
                    if ((x += scaledSpacingX) > r) break;
                }
            }
        }
        if (sizeY + spacingY > 1) {
            final float scaledSizeY = sizeY * s, scaledSpacingY = spacingY * s;
            float y = (ty >= 0.0f ? ty : ty % (scaledSizeY + scaledSpacingY)) + offsetY % (sizeY + spacingY) * s;
            if (y < ty) y += scaledSizeY + scaledSpacingY;
            while (true) {
                if (y >= t) canvas.drawLine(l, y, r, y, paint);
                if ((y += scaledSizeY) > b) break;
                if (spacingY != 0) {
                    if (y >= t) canvas.drawLine(l, y, r, y, paint);
                    if ((y += scaledSpacingY) > b) break;
                }
            }
        }
    }
}
