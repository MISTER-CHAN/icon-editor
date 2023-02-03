package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;

class BitmapHistory {

    private static final int MAX_SIZE = 50;

    private static final Paint PAINT_SRC = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    private static class Node {
        private Bitmap val;
        private Node later;
        private Node earlier;

        private Node(Node earlier, Bitmap val) {
            this.val = val;
            this.earlier = earlier;
            if (earlier != null) {
                earlier.later = this;
            }
        }
    }

    private int size = 0;
    private Node current = null;
    private Node earliest, latest;

    public void add(Bitmap bitmap) {
        if (size > 0) {
            while (latest != current) {
                deleteLatest();
            }
        }
        final Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                bitmap.getConfig(), bitmap.hasAlpha(), bitmap.getColorSpace());
        new Canvas(bm).drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
        current = new Node(current, bm);
        add(current);
        while (size > MAX_SIZE) {
            deleteEarliest();
        }
    }

    private void add(Node node) {
        if (latest == null)
            earliest = node;
        else
            latest.later = node;
        latest = node;
        ++size;
    }

    public boolean canRedo() {
        return current != null && current.later != null;
    }

    public boolean canUndo() {
        return current != null && current.earlier != null;
    }

    public void clear() {
        for (Node n = earliest; n != null; ) {
            final Node later = n.later;
            n.val.recycle();
            n.val = null;
            n.earlier = null;
            n.later = null;
            n = later;
        }
        earliest = latest = null;
        size = 0;
    }

    private void deleteEarliest() {
        if (earliest == null)
            return;
        earliest.val.recycle();
        earliest.val = null;
        earliest = earliest.later;
        if (earliest == null)
            latest = null;
        else
            earliest.earlier = null;
        --size;
    }

    private void deleteLatest() {
        if (latest == null)
            return;
        latest.val.recycle();
        latest.val = null;
        latest = latest.earlier;
        if (latest == null)
            earliest = null;
        else
            latest.later = null;
        --size;
    }

    public Bitmap getCurrent() {
        return Bitmap.createBitmap(current.val);
    }

    public Bitmap redo() {
        current = current.later;
        return Bitmap.createBitmap(current.val);
    }

    public Bitmap undo() {
        current = current.earlier;
        return Bitmap.createBitmap(current.val);
    }
}
