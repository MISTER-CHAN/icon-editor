package com.misterchan.iconeditor;

/**
 * Here is an example of layer tree.<br />
 * <code><nobr>
 * &nbsp; &nbsp;Level 0 &nbsp; &nbsp; &nbsp; &nbsp;Level 1 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Level 2<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ┌───────────────────────────┐<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ┌─┤Layer Mask of Clipped Layer│<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ┌─────────────┐ │ ├───────────────────────────┤<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ┌─┤Clipped Layer├─┴─┤ &nbsp; &nbsp; &nbsp; Clipped Layer &nbsp; &nbsp; &nbsp; │<br />
 * &nbsp; &nbsp;┌──────────┐ │ ├─────────────┤ &nbsp; └───────────────────────────┘<br />
 * &nbsp;┌─┤ Layer 2 &nbsp;│ │ │ Layer Mask &nbsp;│<br />
 * &nbsp;│ ├──────────┤ │ ├─────────────┤<br />
 * &nbsp;│ │ Layer 1 &nbsp;├─┴─┤ &nbsp; Layer 1 &nbsp; │<br />
 * &nbsp;│ ├──────────┤ &nbsp; └─────────────┘<br />
 * ─┴─┤Background│<br />
 * &nbsp; &nbsp;└──────────┘<br />
 * </nobr></code>
 * <br />
 * It should be displayed on layer list like this:<br />
 * <code>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Layer 2<br />
 *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;┌─────┐<br />
 * Layer Mask of Clipped Layer<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ↓<br />
 * &nbsp; &nbsp; &nbsp; Clipped Layer<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;└─────┘<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ↓<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; Layer Mask<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ↓<br />
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Layer 1<br />
 * &nbsp; &nbsp; &nbsp; &nbsp;Background<br />
 * </code><br />
 * <br />
 * In Adobe Photoshop, it should be displayed on layer panel like this:<br />
 * <code><nobr>
 * ┌────────────────────────────────────────────────┐<br />
 * │┌───────┐ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; │<br />
 * ││Layer 2│ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; │<br />
 * │└───────┘ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; │<br />
 * ├────────────────────────────────────────────────┤<br />
 * │ &nbsp; ┌─────────────┐ ┌───────────────────────────┐│<br />
 * │ ┌─│Clipped Layer│ │Layer Mask of Clipped Layer││<br />
 * │ ↓ └─────────────┘ └───────────────────────────┘│<br />
 * ├────────────────────────────────────────────────┤<br />
 * │┌───────┐ ┌──────────┐ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 * ││Layer 1│ │Layer Mask│ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 * │└───────┘ └──────────┘ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 * ├────────────────────────────────────────────────┤<br />
 * │┌──────────┐ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 * ││Background│ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 * │└──────────┘ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 * └────────────────────────────────────────────────┘<br />
 * </nobr></code>
 */
class LayerTree {
    public static class Node {
        public final boolean isRoot;
        public final Layer layer;
        public LayerTree children;
        private Node above;

        public Node(Layer layer) {
            this(layer, false);
        }

        public Node(Layer layer, boolean isRoot) {
            this.isRoot = isRoot;
            this.layer = layer;
        }

        /**
         * @return The brother node above
         */
        public Node getAbove() {
            return above;
        }
    }

    private int size = 0;
    private Node background, foreground;

    public Node getBackground() {
        return background;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Node push(Layer layer) {
        return push(layer, false);
    }

    public Node push(Layer layer, boolean isRoot) {
        final Node node = new Node(layer, isRoot);
        if (foreground == null)
            background = node;
        else
            foreground.above = node;
        foreground = node;
        ++size;
        return node;
    }

    public int size() {
        return size;
    }
}
