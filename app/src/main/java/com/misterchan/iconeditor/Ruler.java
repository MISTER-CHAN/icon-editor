package com.misterchan.iconeditor;

public class Ruler {
    public boolean enabled = false;
    public int startX, startY;
    public int stopX, stopY;

    public void set(int startX, int startY, int stopX, int stopY) {
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
    }
}
