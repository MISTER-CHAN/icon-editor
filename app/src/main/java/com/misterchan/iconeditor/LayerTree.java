package com.misterchan.iconeditor;

import android.graphics.Bitmap;

public class LayerTree {
    private static class Node {
        public Bitmap val;
        public Node branch;
        public Node next;

        private Node(Bitmap val) {
            this.val = val;
        }
    }

    public void offer(Bitmap bitmap) {
    }
}
