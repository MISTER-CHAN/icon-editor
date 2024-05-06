package com.misterchan.iconeditor.tool;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.StringRes;

import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.ui.CoordinateConversions;

public class SelectionTool {
    public enum Position {
        LEFT(R.string.left),
        TOP(R.string.top),
        RIGHT(R.string.right),
        BOTTOM(R.string.bottom);

        @StringRes
        public final int name;

        Position(@StringRes int name) {
            this.name = name;
        }
    }

    private final CoordinateConversions cooConv;
    public Position marqBoundBeingDragged = null;
    public final Rect r = new Rect();

    public SelectionTool(CoordinateConversions cc) {
        cooConv = cc;
    }

    /**
     * Check if the user is dragging a marquee bound.
     */
    public Position checkDraggingMarqueeBound(float viewX, float viewY) {
        marqBoundBeingDragged = null;

        // Marquee Bounds
        final float mbLeft = cooConv.toViewX(r.left), mbTop = cooConv.toViewY(r.top),
                mbRight = cooConv.toViewX(r.right), mbBottom = cooConv.toViewY(r.bottom);

        if (mbLeft - 50.0f <= viewX && viewX < mbLeft + 50.0f) {
            if (mbTop + 50.0f <= viewY && viewY < mbBottom - 50.0f) {

                marqBoundBeingDragged = SelectionTool.Position.LEFT;
            }
        } else if (mbTop - 50.0f <= viewY && viewY < mbTop + 50.0f) {
            if (mbLeft + 50.0f <= viewX && viewX < mbRight - 50.0f) {

                marqBoundBeingDragged = SelectionTool.Position.TOP;
            }
        } else if (mbRight - 50.0f <= viewX && viewX < mbRight + 50.0f) {
            if (mbTop + 50.0f <= viewY && viewY < mbBottom - 50.0f) {

                marqBoundBeingDragged = SelectionTool.Position.RIGHT;
            }
        } else if (mbBottom - 50.0f <= viewY && viewY < mbBottom + 50.0f) {
            if (mbLeft + 50.0f <= viewX && viewX < mbRight - 50.0f) {

                marqBoundBeingDragged = SelectionTool.Position.BOTTOM;
            }
        }

        return marqBoundBeingDragged;
    }

    public boolean dragMarqueeBound(float viewX, float viewY, float scale) {
        final float halfScale = scale / 2.0f;
        if (marqBoundBeingDragged == null) {
            return false;
        }
        switch (marqBoundBeingDragged) {
            case LEFT -> {
                final int left = cooConv.toBitmapX(viewX + halfScale);
                if (left != r.left) r.left = left;
                else return false;
            }
            case TOP -> {
                final int top = cooConv.toBitmapY(viewY + halfScale);
                if (top != r.top) r.top = top;
                else return false;
            }
            case RIGHT -> {
                final int right = cooConv.toBitmapX(viewX + halfScale);
                if (right != r.right) r.right = right;
                else return false;
            }
            case BOTTOM -> {
                final int bottom = cooConv.toBitmapY(viewY + halfScale);
                if (bottom != r.bottom) r.bottom = bottom;
                else return false;
            }
        }
        return true;
    }

    public static void drawMargins(Canvas canvas,
                                   float left, float top, float right, float bottom,
                                   float imageLeft, float imageTop, float imageRight, float imageBottom,
                                   float viewWidth, float viewHeight,
                                   String margLeft, String margTop, String margRight, String margBottom,
                                   Paint paint) {
        final float centerHorizontal = (left + right) / 2.0f, centerVertical = (top + bottom) / 2.0f;
        if (Math.max(left, imageLeft) > 0.0f) {
            canvas.drawLine(left, centerVertical, imageLeft, centerVertical, paint);
            canvas.drawText(margLeft, (imageLeft + left) / 2.0f, centerVertical, paint);
        } else {
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(margLeft, 0.0f, centerVertical, paint);
            paint.setTextAlign(Paint.Align.CENTER);
        }
        if (Math.max(top, imageTop) > 0.0f) {
            canvas.drawLine(centerHorizontal, top, centerHorizontal, imageTop, paint);
            canvas.drawText(margTop, centerHorizontal, (imageTop + top) / 2.0f, paint);
        } else {
            canvas.drawText(margTop, centerHorizontal, -paint.ascent(), paint);
        }
        if (Math.min(right, imageRight) < viewWidth) {
            canvas.drawLine(right, centerVertical, imageRight, centerVertical, paint);
            canvas.drawText(margRight, (imageRight + right) / 2.0f, centerVertical, paint);
        } else {
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(margRight, viewWidth, centerVertical, paint);
            paint.setTextAlign(Paint.Align.CENTER);
        }
        if (Math.min(bottom, imageBottom) < viewHeight) {
            canvas.drawLine(centerHorizontal, bottom, centerHorizontal, imageBottom, paint);
            canvas.drawText(margBottom, centerHorizontal, (imageBottom + bottom) / 2.0f, paint);
        } else {
            canvas.drawText(margBottom, centerHorizontal, viewHeight, paint);
        }
    }

    public void set(int fromX, int fromY, int toX, int toY) {
        if (fromX < toX) {
            r.left = fromX;
            r.right = toX;
        } else {
            r.left = toX - 1;
            r.right = fromX + 1;
        }
        if (fromY < toY) {
            r.top = fromY;
            r.bottom = toY;
        } else {
            r.top = toY - 1;
            r.bottom = fromY + 1;
        }
    }
}
