package com.misterchan.iconeditor;

/**
 * Here is an example of layer tree.<br />
 * <code>
 *     <nobr>
 *         &nbsp; &nbsp;Level 0 &nbsp; &nbsp; &nbsp; &nbsp;Level 1 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Level 2<br />
 *         &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ┌───────────────────────────┐<br />
 *         &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ┌─┤Layer Mask of Clipping Mask│<br />
 *         &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ┌─────────────┐ │ ├───────────────────────────┤<br />
 *         &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ┌─┤Clipping Mask├─┴─┤ &nbsp; &nbsp; &nbsp; Clipping Mask &nbsp; &nbsp; &nbsp; │<br />
 *         &nbsp; &nbsp;┌──────────┐ │ ├─────────────┤ &nbsp; └───────────────────────────┘<br />
 *         &nbsp;┌─┤ Layer 2 &nbsp;│ │ │ Layer Mask &nbsp;│<br />
 *         &nbsp;│ ├──────────┤ │ ├─────────────┤<br />
 *         &nbsp;│ │ Layer 1 &nbsp;├─┴─┤ &nbsp; Layer 1 &nbsp; │<br />
 *         &nbsp;│ ├──────────┤ &nbsp; └─────────────┘<br />
 *         ─┴─┤Background│<br />
 *         &nbsp; &nbsp;└──────────┘<br />
 *     </nobr>
 * </code>
 * <br />
 * The tab bar should display it like this.<br />
 * <code><u>Layer 2</u> &nbsp;<u>[Layer Mask of Clipping Mask →</u> &nbsp;<u>Clipping Mask] →</u> &nbsp;<u>Layer Mask →</u> &nbsp;<u>Layer 1</u> &nbsp;<u>Background ▕</u></code><br />
 * <br />
 * In Adobe Photoshop, it should be displayed on layer panel like this.<br />
 * <code>
 *     <nobr>
 *         ┌────────────────────────────────────────────────┐<br />
 *         │┌───────┐ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; │<br />
 *         ││Layer 2│ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; │<br />
 *         │└───────┘ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; │<br />
 *         ├────────────────────────────────────────────────┤<br />
 *         │ &nbsp; ┌─────────────┐ ┌───────────────────────────┐│<br />
 *         │ ┌─│Clipping Mask│ │Layer Mask of Clipping Mask││<br />
 *         │ ↓ └─────────────┘ └───────────────────────────┘│<br />
 *         ├────────────────────────────────────────────────┤<br />
 *         │┌───────┐ ┌──────────┐ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 *         ││Layer 1│ │Layer Mask│ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 *         │└───────┘ └──────────┘ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 *         ├────────────────────────────────────────────────┤<br />
 *         │┌──────────┐ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 *         ││Background│ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 *         │└──────────┘ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;│<br />
 *         └────────────────────────────────────────────────┘<br />
 *     </nobr>
 * </code>
 */
class LayerTree {
    public static class Node {
        private final Tab val;
        private LayerTree children;
        private Node above;

        public Node(Tab val) {
            this.val = val;
        }

        /**
         * @return The brother node above
         */
        public Node getAbove() {
            return above;
        }

        public LayerTree getChildren() {
            return children;
        }

        public Tab getTab() {
            return val;
        }

        public void setChildren(LayerTree children) {
            this.children = children;
        }
    }

    private int size = 0;
    private Node background, foreground;

    public boolean isEmpty() {
        return size == 0;
    }

    public Node peekBackground() {
        return background;
    }

    public Node push(Tab tab) {
        final Node node = new Node(tab);
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
