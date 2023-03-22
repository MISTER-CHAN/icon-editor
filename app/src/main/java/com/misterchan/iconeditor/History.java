package com.misterchan.iconeditor;

import android.graphics.Bitmap;

class History {
    private static class Node {
        private Bitmap bitmap;
        private Node later;
        private Node earlier;

        private Node(Node earlier, Bitmap bitmap) {
            this.bitmap = bitmap;
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
        current = new Node(current, Bitmap.createBitmap(bitmap));
        add(current);
        while (size > Settings.INST.historyMaxSize()) {
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
            n.bitmap.recycle();
            n.bitmap = null;
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
        earliest.bitmap.recycle();
        earliest.bitmap = null;
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
        latest.bitmap.recycle();
        latest.bitmap = null;
        latest = latest.earlier;
        if (latest == null)
            earliest = null;
        else
            latest.later = null;
        --size;
    }

    public Bitmap getCurrent() {
        return Bitmap.createBitmap(current.bitmap);
    }

    public Bitmap redo() {
        current = current.later;
        return Bitmap.createBitmap(current.bitmap);
    }

    public Bitmap undo() {
        current = current.earlier;
        return Bitmap.createBitmap(current.bitmap);
    }
}
