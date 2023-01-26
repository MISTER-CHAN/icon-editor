package com.misterchan.iconeditor;

class LayerTree {
    public static class Node {
        private final Tab val;
        private LayerTree branch;
        private Node above;

        public Node(Tab val) {
            this.val = val;
        }

        public Node getAbove() {
            return above;
        }

        public LayerTree getBranch() {
            return branch;
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
            foreground.above = node;
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
