package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.widget.Toast;

import java.util.Deque;
import java.util.LinkedList;

class BitmapHistory {

    private static final Paint PAINT = new Paint();

    private static class Node {
        private final Bitmap val;
        private Node next;
        private final Node prev;

        private Node(Bitmap val, Node prev) {
            this.val = val;
            this.prev = prev;
            if (prev != null) {
                prev.next = this;
            }
        }
    }

    private final Deque<Node> history = new LinkedList<>();
    private Node current = null;

    boolean canRedo() {
        return current != null && current.next != null;
    }

    boolean canUndo() {
        return current != null && current.prev != null;
    }

    Bitmap getCurrent() {
        return current.val;
    }

    void offer(Bitmap bitmap) {
        if (!history.isEmpty()) {
            Node node;
            while ((node = history.peekFirst()) != current) {
                if (node != null) {
                    node.val.recycle();
                }
                history.removeFirst();
            }
        }
        Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(bm).drawBitmap(bitmap, 0.0f, 0.0f, PAINT);
        history.offerFirst(current = new Node(bm, current));
    }

    void recycle() {
        Node node;
        while (!history.isEmpty()) {
            if ((node = history.peekFirst()) != null) {
                node.val.recycle();
            }
            history.removeFirst();
        }
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
