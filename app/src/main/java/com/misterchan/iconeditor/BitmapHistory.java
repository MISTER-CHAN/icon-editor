package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Paint;

class BitmapHistory {

    private static final int MAX_SIZE = 50;

    private static final Paint PAINT = new Paint();

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
        return current.val;
    }

    public void offer(Bitmap bitmap) {
        if (size > 0) {
            while (latest != current) {
                deleteLatest();
            }
        }
        offer(current = new Node(current, Bitmap.createBitmap(bitmap)));
        while (size > MAX_SIZE) {
            deleteEarliest();
        }
    }

    private void offer(Node node) {
        if (latest == null)
            earliest = node;
        else
            latest.later = node;
        latest = node;
        ++size;
    }

    public Bitmap redo() {
        current = current.later;
        return current.val;
    }

    public Bitmap undo() {
        current = current.earlier;
        return current.val;
    }
}
