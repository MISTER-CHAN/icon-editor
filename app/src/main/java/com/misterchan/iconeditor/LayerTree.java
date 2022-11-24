package com.misterchan.iconeditor;

class LayerTree {
    static class Node {
        private Tab val;
        private LayerTree branch;
        private Node front;

        public Node(Tab val) {
            this.val = val;
        }

        public LayerTree getBranch() {
            return branch;
        }

        public Node getFront() {
            return front;
        }

        public Tab getTab() {
            return val;
        }

        public void setBranch(LayerTree branch) {
            this.branch = branch;
        }
    }

    private int size = 0;
    private Node background, foreground;

    public boolean isEmpty() {
        return size == 0;
    }

    public Node offer(Tab tab) {
        final Node node = new Node(tab);
        if (foreground == null)
            background = node;
        else
            foreground.front = node;
        foreground = node;
        ++size;
        return node;
    }

    public Node peekBackground() {
        return background;
    }

    public int size() {
        return size;
    }
}
