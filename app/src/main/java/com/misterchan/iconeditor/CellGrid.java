package com.misterchan.iconeditor;

import androidx.annotation.IntRange;

public class CellGrid {
    public boolean enabled = false;

    @IntRange(from = 0)
    public int sizeX = 16, sizeY = 16;

    @IntRange(from = 0)
    public int spacingX = 0, spacingY = 0;

    public int offsetX = 0, offsetY = 0;
}
