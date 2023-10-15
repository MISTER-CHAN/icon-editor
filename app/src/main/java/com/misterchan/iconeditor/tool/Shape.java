package com.misterchan.iconeditor.tool;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface Shape {
    Rect mapRect(int x0, int y0, int x1, int y1);

    String drawShapeOntoCanvas(Canvas canvas, int x0, int y0, int x1, int y1);
}
