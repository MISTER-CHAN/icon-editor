package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

class BitmapHistory {

    private static final int MAX_SIZE = 50;

    private static final Paint PAINT = new Paint();

    private static class Node {
        private Bitmap val;
        private Node next;
        private Node prev;

        private Node(Bitmap val, Node prev) {
            this.val = val;
            this.prev = prev;
            if (prev != null) {
                prev.next = this;
            }
        }
    }

    private int size = 0;
    private Node current = null;
    private Node first, last;

    boolean canRedo() {
        return current != null && current.next != null;
    }

    boolean canUndo() {
        return current != null && current.prev != null;
    }

    void clear() {
        for (Node n = first; n != null; ) {
            final Node next = n.next;
            n.val.recycle();
            n.val = null;
            n.prev = null;
            n.next = null;
            n = next;
        }
        first = last = null;
        size = 0;
    }

    private void delete() {
        if (last == null)
            return;
        last.val.recycle();
        last.val = null;
        last = last.prev;
        if (last == null)
            first = null;
        else
            last.next = null;
        --size;
    }

    Bitmap getCurrent() {
        return current.val;
    }

    void offer(Bitmap bitmap) {
        if (size > 0) {
            while (last != current) {
                delete();
            }
        }
        Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(bm).drawBitmap(bitmap, 0.0f, 0.0f, PAINT);
        offer(current = new Node(bm, current));
        while (size > MAX_SIZE) {
            delete();
        }
    }

    private void offer(Node node) {
        if (last == null)
            first = node;
        else
            last.next = node;
        last = node;
        ++size;
    }

    Bitmap redo() {
        current = current.next;
        return current.val;
    }

    Bitmap undo() {
        current = current.prev;
        return current.val;
    }
}
