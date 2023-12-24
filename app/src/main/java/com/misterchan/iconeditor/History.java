package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <code><nobr>
 * Earliest&#x250C; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510;<br />
 * &nbsp; &nbsp; Node&#x2514; &#x2502;Undo Action&#x2502; &#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518; &#x2502; Rect<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510; &#x2502;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x250C; &#x2502;Redo Action&#x2502; &#x2518;<br />
 * &nbsp; &nbsp; Node&#x2502; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2502; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2514; &#x2502;Undo Action&#x2502; &#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518; &#x2502; Rect<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510; &#x2502;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x250C; &#x2502;Redo Action&#x2502; &#x2518;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2502; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518;<br />
 * <br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x22EE;<br />
 * <br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2502; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &#x2514; &#x2502;Undo Action&#x2502; &#x2510;<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x252C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518; &#x2502; Rect<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &#x250C;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2534;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2510; &#x2502;<br />
 * &nbsp; Latest&#x250C; &#x2502;Redo Action&#x2502; &#x2518;<br />
 * &nbsp; &nbsp; Node&#x2514; &#x2514;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2500;&#x2518;<br />
 * </nobr></code>
 */
public class History {
    public record Action(Layer layer, @Nullable Rect rect, Bitmap bm) {
        private void recycle() {
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
            if (!r.intersect(0, 0, src.getWidth(), src.getHeight())) return;
            bm = Bitmap.createBitmap(src, r.left, r.top, r.width(), r.height());
        }
        while (latest != current) {
            deleteLatest();
        }
        if (current.undoAction != null) current.undoAction.recycle();
        current.undoAction = new Action(layer, r, bm);
    }

    public synchronized Rect addRedoAction(Layer layer, Bitmap src) {
        if (Settings.INST.historyMaxSize() <= 0) return null;
        if (size <= 0) {
            current = new Node(null, null);
            add(current);
            return null;
        }
        if (current.undoAction == null) return null;

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
        if (current == null || current.later == null) {
            return false;
        }
        while (current.later.redoAction.layer.bitmap.isRecycled()) {
            current.later.redoAction.recycle();
            current.undoAction.recycle();
            current.undoAction = current.later.undoAction;
            current.later = current.later.later;
            if (current.later != null) {
                current.later.earlier = current;
            } else {
                latest = current;
                return false;
            }
        }
        return true;
    }

    public boolean canUndo() {
        if (current == null || current.earlier == null) {
            return false;
        }
        while (current.earlier.undoAction.layer.bitmap.isRecycled()) {
            current.earlier.undoAction.recycle();
            current.redoAction.recycle();
            current.redoAction = current.earlier.redoAction;
            current.earlier = current.earlier.earlier;
            if (current.earlier != null) {
                current.earlier.later = current;
            } else {
                earliest = current;
                return false;
            }
        }
        return true;
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

    public Action redo() {
        current = current.later;
        return current.redoAction;
    }

    public Action undo() {
        current = current.earlier;
        return current.undoAction;
    }
}
