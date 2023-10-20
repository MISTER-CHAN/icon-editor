package com.misterchan.iconeditor.tool;

import android.graphics.Rect;

import androidx.annotation.StringRes;

import com.misterchan.iconeditor.R;

public class SelectionTool {
    public interface CoordinateConversions {
        int toBitmapX(float x);

        int toBitmapY(float y);

        float toViewX(int x);

        float toViewY(int y);
    }

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
    public Position checkDraggingMarqueeBound(float x, float y) {
        marqBoundBeingDragged = null;

        // Marquee Bounds
        final float mbLeft = cooConv.toViewX(r.left), mbTop = cooConv.toViewY(r.top),
                mbRight = cooConv.toViewX(r.right), mbBottom = cooConv.toViewY(r.bottom);

        if (mbLeft - 50.0f <= x && x < mbLeft + 50.0f) {
            if (mbTop + 50.0f <= y && y < mbBottom - 50.0f) {

                marqBoundBeingDragged = SelectionTool.Position.LEFT;
            }
        } else if (mbTop - 50.0f <= y && y < mbTop + 50.0f) {
            if (mbLeft + 50.0f <= x && x < mbRight - 50.0f) {

                marqBoundBeingDragged = SelectionTool.Position.TOP;
            }
        } else if (mbRight - 50.0f <= x && x < mbRight + 50.0f) {
            if (mbTop + 50.0f <= y && y < mbBottom - 50.0f) {

                marqBoundBeingDragged = SelectionTool.Position.RIGHT;
            }
        } else if (mbBottom - 50.0f <= y && y < mbBottom + 50.0f) {
            if (mbLeft + 50.0f <= x && x < mbRight - 50.0f) {

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
}
