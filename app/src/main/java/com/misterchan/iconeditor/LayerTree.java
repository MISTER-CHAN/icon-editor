package com.misterchan.iconeditor;

class LayerTree {
    static class Node {
        private Tab val;
        private LayerTree branch;
        private Node front;

        private Node(Tab val) {
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

        public void setFront(Node front) {
            this.front = front;
        }
    }

    private int size;
    private Node background;

    public void offer(Tab tab) {
        ++size;
    }

    public Node peekBackground() {
        return background;
    }
}
