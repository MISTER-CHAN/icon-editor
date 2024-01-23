package com.misterchan.iconeditor.ui;

public interface CoordinateConversions {
    int toBitmapX(float x);

    int toBitmapY(float y);

    float toViewX(int x);

    float toViewX(float x);

    float toViewY(int y);

    float toViewY(float y);
}
