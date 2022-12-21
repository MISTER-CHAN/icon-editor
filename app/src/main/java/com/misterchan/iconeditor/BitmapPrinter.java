package com.misterchan.iconeditor;

import android.graphics.Canvas;
import android.graphics.Paint;

interface BitmapPrinter {
    void run(Canvas canvas, Tab tab, Paint paint);
}