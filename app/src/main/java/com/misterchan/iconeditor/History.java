package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * <code><nobr>
 * Earliest&#x250C; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510;<br />
 * &nbsp; &nbsp; Node&#x2514; &#x2502;Undo Action&#x2502; &#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518; &#x2502;Rect<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510; &#x2502;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x250C; &#x2502;Redo Action&#x2502; &#x2518;<br />
 * &nbsp; &nbsp; Node&#x2502; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2502; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2514; &#x2502;Undo Action&#x2502; &#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518; &#x2502;Rect<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510; &#x2502;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x250C; &#x2502;Redo Action&#x2502; &#x2518;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2502; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518;<br />
 * <br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x22EE;<br />
 * <br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2502; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2514; &#x2502;Undo Action&#x2502; &#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518; &#x2502;Rect<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510; &#x2502;<br />
 * &nbsp; Latest&#x250C; &#x2502;Redo Action&#x2502; &#x2518;<br />
 * &nbsp; &nbsp; Node&#x2514; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518;<br />
 * </nobr></code>
 */
public class History {
    public record Action(Layer layer, Rect rect, Bitmap bm) {
        public void recycle() {
            bm.recycle();
        }
    }

    private static class Node {
        private Node earlier, later;
        private Action redoAction, undoAction;

        private Node(Node earlier, Action redoAction) {
            this.redoAction = redoAction;
            this.earlier = earlier;
            if (earlier != null) {
                earlier.later = this;
            }
        }

        private void recycle() {
            if (redoAction != null) redoAction.recycle();
            if (undoAction != null) undoAction.recycle();
        }
    }

    private boolean closed = false;
    private int size = 0;
    private Node current = null;
    private Node earliest, latest;

    public synchronized void addUndoAction(Layer layer, Bitmap src, Rect rect) {
        if (Settings.INST.historyMaxSize() <= 0) return;

        Bitmap bm;
        Rect r;
        if (rect == null) {
            r = null;
            bm = Bitmap.createBitmap(src);
        } else {
            r = new Rect(rect);
            if (!r.intersect(0, 0, src.getWidth(), src.getHeight())) {
                closed = true;
                return;
            }
            bm = Bitmap.createBitmap(src, r.left, r.top, r.width(), r.height());
        }

        while (latest != current) {
            deleteLatest();
        }

        if (current.undoAction != null) current.undoAction.recycle();
        current.undoAction = new Action(layer, r, bm);
    }

    public synchronized Rect addRedoAction(Layer layer, Bitmap src) {
        if (Settings.INST.historyMaxSize() <= 0 || size <= 0 || current.undoAction == null) {
            return null;
        }
        if (closed) {
            closed = false;
            return null;
        }

        Rect rect = current.undoAction.rect;
        Bitmap bm = rect == null ? Bitmap.createBitmap(src) : Bitmap.createBitmap(src, rect.left, rect.top, rect.width(), rect.height());
        current = new Node(current, new Action(layer, rect, bm));
        add(current);

        while (size > Settings.INST.historyMaxSize()) {
            deleteEarliest();
        }
        return rect;
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
            Node later = n.later;
            n.recycle();
            n.earlier = n.later = null;
            n = later;
        }
        earliest = latest = null;
        size = 0;
    }

    private void deleteEarliest() {
        if (earliest == null)
            return;
        earliest.undoAction.recycle();
        earliest.undoAction = null;
        earliest = earliest.later;
        if (earliest == null) {
            latest = null;
        } else {
            earliest.earlier = null;
            earliest.redoAction.recycle();
            earliest.redoAction = null;
        }
        --size;
    }

    private void deleteLatest() {
        if (latest == null)
            return;
        latest.redoAction.recycle();
        latest = latest.earlier;
        if (latest == null) {
            earliest = null;
        } else {
            latest.later = null;
            latest.undoAction.recycle();
            latest.undoAction = null;
        }
        --size;
    }

    public void init() {
        current = new Node(null, null);
        add(current);
    }

    public synchronized void optimize() {
        Node n = earliest;
        for (; n.undoAction != null; n = n.later) {
            if (!n.undoAction.layer.bitmap.isRecycled()) continue;
            n.undoAction.recycle();
            n.undoAction = n.later.undoAction;
            n.later.redoAction.recycle();
            if (n.later == current) current = n;
            n.later = n.later.later;
            if (n.later == null) break;
            n.later.earlier = n;
        }
        latest = n;
    }

    public Action redo() {
        current = current.later;
        return current.redoAction;
    }

    public Action undo() {
        current = current.earlier;
        return current.undoAction;
    }
}
