package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.annotation.StringRes;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

class Tab {
    public enum Filter {
        COLOR_MATRIX, CURVES, HSV
    }

    private static final Paint PAINT_SRC = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    private static final Paint PAINT_SRC_OVER = new Paint();

    public boolean drawBelow = false;
    public boolean isBackground = true;
    public boolean visible = false;
    public Bitmap bitmap;
    public Bitmap.CompressFormat compressFormat;
    public final BitmapHistory history = new BitmapHistory();
    public final CellGrid cellGrid = new CellGrid();
    private CheckBox cbVisible;
    public final Deque<Point> guides = new LinkedList<>();
    public Filter filter;
    public float scale;
    public float translationX, translationY;
    private int level = 0;
    public int left = 0, top = 0;
    public final Paint paint = new Paint();
    public String path;
    private Tab background;
    private int backgroundPosition;
    private TextView tvBackground;
    private TextView tvLowerLevel, tvRoot, tvParent, tvLeaf;
    private TextView tvTitle;

    @Size(20)
    public float[] colorMatrix = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    @Size(5)
    public int[][] curves = {new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100]};

    @Size(3)
    public float[] deltaHsv = new float[]{0.0f, 0.0f, 0.0f};

    {
        for (int i = 0; i <= 4; ++i) {
            for (int j = 0x0; j < 0x100; ++j) {
                curves[i][j] = j;
            }
        }
    }

    private static void addFilters(Bitmap bitmap, Tab tab) {
        switch (tab.filter) {
            case COLOR_MATRIX:
                BitmapUtil.addColorMatrixColorFilter(
                        bitmap, 0, 0, bitmap, 0, 0, tab.colorMatrix);
                break;
            case CURVES:
                BitmapUtil.applyCurves(bitmap, tab.curves);
                break;
            case HSV:
                BitmapUtil.shiftHsv(bitmap, tab.deltaHsv);
                break;
        }
    }

    private void clearLevelIcons() {
        tvLowerLevel.setText("");
        tvRoot.setText("");
        tvParent.setText("");
        tvLeaf.setText("");
    }

    public static LayerTree computeLayerTree(List<Tab> tabs, int pos) {
        final Stack<LayerTree> stack = new Stack<>();
        LayerTree layerTree = new LayerTree();
        LayerTree.Node prev = null;

        final Tab projBegin = findBackground(tabs, pos);
        final int first = tabs.size() - 1; // last = 0
        Tab background = tabs.get(first);
        int backgroundPos = first;
        background.isBackground = true;
        boolean isInProject = false;
        Tab lastTabInProject = null;
        stack.push(layerTree);
        for (int i = first; i >= 0; --i) {
            final Tab t = tabs.get(i);
            if (t.isBackground) {
                t.showBackground();
                background = t;
                backgroundPos = i;
                isInProject = t == projBegin;
            }
            t.background = background;
            t.backgroundPosition = backgroundPos;
            if (isInProject) {
                t.cbVisible.setVisibility(View.VISIBLE);
                t.clearLevelIcons();
                if (t.level > 0) {
                    t.tvLowerLevel.setText("→");
                }
                lastTabInProject = t;
                // If current layer is background layer
                if (prev == null) {
                    for (int j = 1; j < t.level; j++) {
                        t.tvRoot.append("]");
                    }
                    prev = layerTree.push(t);
                    continue;
                }
            } else {
                t.cbVisible.setVisibility(View.GONE);
                continue;
            }
            final Tab prevTab = prev.getTab();
            final int levelDiff = t.level - prevTab.level;

            if (levelDiff == 0) {
                prev = stack.peek().push(t);

            } else if (levelDiff > 0) {
                LayerTree lt = null;
                for (int j = 0; j < levelDiff; ++j) {
                    lt = new LayerTree();
                    prev.setChildren(lt);
                    prev = lt.push(prevTab);
                    stack.push(lt);
                }
                for (int j = prevTab.level > 0 ? 0 : 1; j < levelDiff; ++j) {
                    prevTab.tvParent.append("]");
                }
                prev = lt.push(t);

            } else /* if (levelDiff < 0) */ {
                // If current level is lower than or equals to background's level
                if (-levelDiff < stack.size()) {
                    for (int j = 0; j > levelDiff; --j) {
                        stack.pop();
                    }
                    for (int j = t.level > 0 ? 0 : -1; j > levelDiff; --j) {
                        prevTab.tvLeaf.append("[");
                    }
                    prev = stack.peek().push(t);
                } else {
                    // Re-compute layer tree
                    stack.clear();
                    layerTree = stack.push(new LayerTree());
                    for (int j = t.level > 0 ? 0 : -1; j > levelDiff; --j) {
                        prevTab.tvLeaf.append("[");
                    }
                    prev = layerTree.push(t);
                }

            }
        }
        for (int i = lastTabInProject.level; i > 1; --i) {
            lastTabInProject.tvLeaf.append("[");
        }

        return layerTree;
    }

    private static Tab findBackground(List<Tab> tabs, int pos) {
        Tab t = null;
        for (int i = pos; i < tabs.size(); ++i) {
            t = tabs.get(i);
            if (t.isBackground) {
                break;
            }
        }
        return t;
    }

    public static Tab getAbove(List<Tab> tabs, int pos) {
        if (pos <= 0) {
            return null;
        }
        final Tab t = tabs.get(pos - 1);
        return t.isBackground ? null : t;
    }

    public static Tab getBelow(List<Tab> tabs, int pos) {
        final int newPos = pos + 1;
        if (newPos >= tabs.size()) {
            return null;
        }
        final Tab newTab = tabs.get(newPos);
        return tabs.get(pos).getBackground() == newTab.getBackground() ? newTab : null;
    }

    public Tab getBackground() {
        return background;
    }

    public int getBackgroundPosition() {
        return backgroundPosition;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        final String s = tvTitle.getText().toString();
        final int i = s.lastIndexOf('.');
        return i == -1 ? s : s.substring(0, i);
    }

    public void initViews(View view) {
        cbVisible = view.findViewById(R.id.cb_visible);
        tvRoot = view.findViewById(R.id.tv_root);
        tvParent = view.findViewById(R.id.tv_parent);
        tvLeaf = view.findViewById(R.id.tv_leaf);
        tvBackground = view.findViewById(R.id.tv_background);
        tvLowerLevel = view.findViewById(R.id.tv_lower_level);
        tvTitle = view.findViewById(R.id.tv_title);
    }

    public void levelDown() {
        ++level;
    }

    public static void levelDown(List<Tab> tabs, int position) {
        final Tab tab = tabs.get(position);
        final int level = tab.level++;
        for (int i = position - 1; i >= 0; --i) {
            final Tab t = tabs.get(i);
            if (t.isBackground) {
                break;
            }
            if (t.level > level) {
                ++t.level;
            } else {
                break;
            }
        }
    }

    public void levelUp() {
        if (level <= 0) {
            return;
        }
        --level;
    }

    public static void mergeLayers(Tab upper, Tab lower) {
        final Bitmap uBm = Bitmap.createBitmap(upper.bitmap.getWidth(), upper.bitmap.getHeight(),
                upper.bitmap.getConfig(), upper.bitmap.hasAlpha(), upper.bitmap.getColorSpace());
        final Bitmap lBm = lower.bitmap;
        final Canvas uCv = new Canvas(uBm), lCv = new Canvas(lBm);
        uCv.drawBitmap(upper.drawBelow ? lBm : upper.bitmap, 0.0f, 0.0f, PAINT_SRC);
        if (upper.filter != null) {
            addFilters(uBm, upper);
        }
        lCv.drawBitmap(uBm, upper.left - lower.left, upper.top - lower.top, upper.paint);
    }

    public static Bitmap mergeLayers(final LayerTree tree, final Rect rect) {
        return mergeLayers(tree, rect, null, null, null);
    }

    public static Bitmap mergeLayers(final LayerTree tree, final Rect rect,
                                     final Tab excludedTab, final Bitmap excludedBitmap, final Tab extraExclTab) {
        return mergeLayers(tree, rect, null, excludedTab, excludedBitmap, extraExclTab);
    }

    public static Bitmap mergeLayers(final LayerTree tree, final Rect rect, final Bitmap background,
                                     final Tab excludedTab, final Bitmap excludedBitmap, final Tab extraExclTab) {
        final LayerTree.Node backgroundNode = tree.getBackground();
        final Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        if (background != null) {
            canvas.drawBitmap(background, 0.0f, 0.0f, PAINT_SRC);
        }

        try {
            for (LayerTree.Node node = backgroundNode; node != null; node = node.getAbove()) {
                final Tab tab = node.getTab();
                if (!tab.visible && tab != excludedTab) {
                    continue;
                }
                final LayerTree children = node.getChildren();
                if (children == null) {
                    final Paint paint = node == backgroundNode && background == null ? PAINT_SRC : tab.paint;
                    final int bmW = tab.bitmap.getWidth(), bmH = tab.bitmap.getHeight();
                    // Rectangle src and dst are intersection between background layer subset and current layer
                    final Rect src = new Rect(0, 0, bmW, bmH);
                    final int srcOrigLeft = -tab.left, srcOrigTop = -tab.top; // Origin location related to layer
                    if (!src.intersect(srcOrigLeft + rect.left, srcOrigTop + rect.top, srcOrigLeft + rect.right, srcOrigTop + rect.bottom)) {
                        continue; // No intersection
                    }
                    final Rect dst = new Rect(0, 0, rect.width(), rect.height());
                    final int dstLeft = -rect.left + tab.left, dstTop = -rect.top + tab.top; // Layer location related to background layer subset
                    dst.intersect(dstLeft, dstTop, dstLeft + bmW, dstTop + bmH);
                    final int intW = src.width(), intH = src.height(); // Intersection size, src size == dst size
                    final Rect intRel = new Rect(0, 0, intW, intH); // Intersection relative rectangle

                    Rect ees = null, eed = null; // Intersection between intersection and extra excluded layer
                    if (tab == excludedTab && extraExclTab != null) {
                        final int w = extraExclTab.bitmap.getWidth(), h = extraExclTab.bitmap.getHeight();
                        ees = new Rect(0, 0, w, h);
                        final int sol = -extraExclTab.left - tab.left + rect.left, sot = -extraExclTab.top - tab.top + rect.top; // Origin location related to extra excluded layer
                        if (ees.intersect(sol + dst.left, sot + dst.top, sol + dst.right, sot + dst.bottom)) {
                            eed = new Rect(intRel);
                            final int dl = -dst.left - rect.left + tab.left + extraExclTab.left, dt = -dst.top - rect.top + tab.top + extraExclTab.top; // Extra excluded location related to intersection
                            eed.intersect(dl, dt, dl + w, dt + h);
                        }
                    }

                    if (tab.filter == null) {
                        if (tab.drawBelow) {
                            canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
                        } else if (tab == excludedTab) {
                            if (eed != null) {
                                final Bitmap bm = Bitmap.createBitmap(intW, intH, Bitmap.Config.ARGB_8888); // Intersection bitmap
                                final Canvas cv = new Canvas(bm);
                                try {
                                    cv.drawBitmap(excludedBitmap, src, intRel, PAINT_SRC);
                                    cv.drawBitmap(extraExclTab.bitmap, ees, eed, PAINT_SRC_OVER);
                                    canvas.drawBitmap(bm, intRel, dst, paint);
                                } finally {
                                    bm.recycle();
                                }
                            } else {
                                canvas.drawBitmap(excludedBitmap, src, dst, paint);
                            }
                        } else {
                            canvas.drawBitmap(tab.bitmap, src, dst, paint);
                        }
                    } else {
                        final Bitmap bm = Bitmap.createBitmap(intW, intH, Bitmap.Config.ARGB_8888); // Intersection bitmap
                        final Canvas cv = new Canvas(bm);
                        try {
                            if (tab.drawBelow) {
                                cv.drawBitmap(bitmap, dst, intRel, PAINT_SRC);
                            } else if (tab == excludedTab) {
                                cv.drawBitmap(excludedBitmap, src, intRel, PAINT_SRC);
                                if (eed != null) {
                                    cv.drawBitmap(extraExclTab.bitmap, ees, eed, PAINT_SRC_OVER);
                                }
                            } else {
                                cv.drawBitmap(tab.bitmap, src, intRel, PAINT_SRC);
                            }
                            addFilters(bm, tab);
                            canvas.drawBitmap(bm, intRel, dst, paint);
                        } finally {
                            bm.recycle();
                        }
                    }
                } else {
                    final Bitmap branchBitmap = mergeLayers(children, rect,
                            !tab.drawBelow || node == backgroundNode ? null : bitmap,
                            excludedTab, excludedBitmap, extraExclTab);
                    canvas.drawBitmap(branchBitmap, 0.0f, 0.0f, tab.paint);
                    branchBitmap.recycle();
                }
            }
        } catch (RuntimeException e) {
            bitmap.recycle();
            throw e;
        }

        return bitmap;
    }

    public void moveBy(int dx, int dy) {
        left += dx;
        top += dy;
    }

    public void moveTo(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public void setLevel(@IntRange(from = 0) int level) {
        if (level < 0) {
            return;
        }
        this.level = level;
    }

    /**
     * @param listener Listener on {@link #cbVisible} checked change
     */
    public void addOVCBCCListener(CompoundButton.OnCheckedChangeListener listener) {
        cbVisible.setOnCheckedChangeListener(listener);
    }

    public void removeOLVCBCCListener() {
        cbVisible.setOnCheckedChangeListener(null);
    }

    public void setTitle(@StringRes int resId) {
        tvTitle.setText(resId);
    }

    public void setVisible(boolean visible) {
        cbVisible.setChecked(visible);
    }

    public void setTitle(CharSequence text) {
        tvTitle.setText(text);
    }

    private void showBackground() {
        if (tvBackground == null) {
            return;
        }
        final StringBuilder sb = new StringBuilder();
        if (isBackground) {
            sb.append('▕');
        }
        tvBackground.setText(sb);
    }

    public void showTo(View view) {
        ((TextView) view.findViewById(R.id.tv_parent)).setText(tvParent.getText());
        ((TextView) view.findViewById(R.id.tv_leaf)).setText(tvLeaf.getText());
        ((TextView) view.findViewById(R.id.tv_root)).setText(tvRoot.getText());
        ((TextView) view.findViewById(R.id.tv_background)).setText(tvBackground.getText());
        ((TextView) view.findViewById(R.id.tv_lower_level)).setText(tvLowerLevel.getText());
        ((TextView) view.findViewById(R.id.tv_title)).setText(tvTitle.getText());
    }
}
