package com.misterchan.iconeditor;

public interface Shape {

    void drawBitmapOnView(int x0, int y0, int x1, int y1);

    void drawShapeOnCanvas(int x0, int y0, int x1, int y1);

    String drawShapeOnView(int x0, int y0, int x1, int y1);
}
