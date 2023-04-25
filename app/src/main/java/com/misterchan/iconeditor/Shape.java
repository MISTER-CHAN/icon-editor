package com.misterchan.iconeditor;

interface Shape {

    void drawBitmapOntoView(int x0, int y0, int x1, int y1);

    void drawShapeIntoImage(int x0, int y0, int x1, int y1);

    String drawShapeOntoView(int x0, int y0, int x1, int y1);
}
