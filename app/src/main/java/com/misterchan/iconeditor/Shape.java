package com.misterchan.iconeditor;

interface Shape {

    void drawImageOnView(int x0, int y0, int x1, int y1);

    void drawShapeOnImage(int x0, int y0, int x1, int y1);

    String drawShapeOnView(int x0, int y0, int x1, int y1);
}
