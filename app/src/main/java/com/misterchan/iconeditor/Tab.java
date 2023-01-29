package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.Size;

import java.util.List;
import java.util.Stack;

class Tab {
    public enum Filter {
        COLOR_MATRIX, CURVES, HSV
    }

    private static final Paint PAINT_SRC = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
        }
    };

    private static final Paint PAINT_SRC_OVER = new Paint();

    public boolean drawBelow = false;
    public boolean isBackground = true;
    public boolean visible = false;
    public Bitmap bitmap;
    public Bitmap.CompressFormat compressFormat;
    public BitmapHistory history;
    public CellGrid cellGrid;
    public CheckBox cbLayerVisible;
    public Filter filter;
    public float scale;
    public float translationX, translationY;
    private int level = 0;
    public int left = 0, top = 0;
    public Paint paint;
    public String path;
    private Tab background;
    public TextView tvLayerLevel;
    public TextView tvTitle;

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

    public static LayerTree computeLayerTree(List<Tab> tabs, int pos) {
        final Stack<LayerTree> stack = new Stack<>();
        LayerTree layerTree = new LayerTree();
        LayerTree.Node prev = null;

        final Tab projBegin = findBackground(tabs, pos);
        final int last = tabs.size() - 1;
        Tab background = tabs.get(last);
        boolean isInProject = false;
        stack.push(layerTree);
        for (int i = last; i >= 0; --i) {
            final Tab t = tabs.get(i);
            if (t.isBackground) {
                background = t;
                isInProject = t == projBegin;
            }
            t.background = background;
            if (isInProject) {
                t.cbLayerVisible.setVisibility(View.VISIBLE);
                if (prev == null) {
                    prev = layerTree.offer(t);
                    continue;
                }
            } else {
                t.cbLayerVisible.setVisibility(View.GONE);
                continue;
            }
            final Tab prevTab = prev.getTab();
            final int levelDiff = t.level - prevTab.level;

            if (levelDiff == 0) {
                prev = stack.peek().offer(t);

            } else if (levelDiff > 0) {
                LayerTree lt = null;
                for (int j = 0; j < levelDiff; ++j) {
                    lt = new LayerTree();
                    prev.setBranch(lt);
                    prev = lt.offer(prevTab);
                    stack.push(lt);
                }
                prev = lt.offer(t);

            } else /* if (levelDiff < 0) */ {
                if (-levelDiff < stack.size()) {
                    for (int j = 0; j > levelDiff; --j) {
                        stack.pop();
                    }
                    prev = stack.peek().offer(t);
                } else {
                    stack.clear();
                    layerTree = stack.push(new LayerTree());
                    prev = layerTree.offer(t);
                }

            }
        }

        return layerTree;
    }

    public static Tab findBackground(List<Tab> tabs, int pos) {
        Tab t = null;
        for (int i = pos; i < tabs.size(); ++i) {
            t = tabs.get(i);
            if (t.isBackground) {
                break;
            }
        }
        return t;
    }

    public Tab getBackground() {
        return background;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        final String s = tvTitle.getText().toString();
        final int i = s.lastIndexOf('.');
        return i == -1 ? s : s.substring(0, i);
    }

    public void levelDown() {
        ++level;
        showLayerLevel();
    }

    public static void levelDown(List<Tab> tabs, int position) {
        final Tab tab = tabs.get(position);
        final int level = tab.level++;
        tab.showLayerLevel();
        for (int i = position - 1; i >= 0; --i) {
            final Tab t = tabs.get(i);
            if (t.isBackground) {
                break;
            }
            if (t.level > level) {
                ++t.level;
                t.showLayerLevel();
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
        showLayerLevel();
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
        lCv.drawBitmap(uBm, 0.0f, 0.0f, upper.paint);
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
        final int w = rect.width(), h = rect.height();
        final Rect relative = new Rect(0, 0, w, h);
        final LayerTree.Node root = tree.peekBackground();
        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        if (background != null) {
            canvas.drawBitmap(background, 0.0f, 0.0f, PAINT_SRC);
        }

        try {
            for (LayerTree.Node node = root; node != null; node = node.getAbove()) {
                final Tab tab = node.getTab();
                if (!tab.visible && tab != excludedTab) {
                    continue;
                }
                final LayerTree branch = node.getBranch();
                if (branch == null) {
                    final Paint paint = node == root && background != null ? PAINT_SRC : tab.paint;
                    final Rect r = new Rect(rect);
                    r.offset(-tab.left, -tab.top);
                    if (tab.filter == null) {
                        if (tab.drawBelow) {
                            canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
                        } else if (tab == excludedTab) {
                            canvas.drawBitmap(excludedBitmap, r, relative, paint);
                        } else {
                            canvas.drawBitmap(tab.bitmap, r, relative, paint);
                        }
                    } else {
                        final Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                        final Canvas cv = new Canvas(bm);
                        try {
                            if (tab.drawBelow) {
                                cv.drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
                            } else if (tab == excludedTab) {
                                cv.drawBitmap(excludedBitmap, r, relative, PAINT_SRC);
                            } else {
                                cv.drawBitmap(tab.bitmap, r, relative, PAINT_SRC);
                            }
                            addFilters(bm, tab);
                            canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
                        } finally {
                            bm.recycle();
                        }
                    }
                } else {
                    final Bitmap branchBitmap = mergeLayers(branch, rect,
                            !tab.drawBelow || node == root ? null : bitmap,
                            excludedTab, excludedBitmap, extraExclTab);
                    canvas.drawBitmap(branchBitmap, 0.0f, 0.0f, tab.paint);
                    branchBitmap.recycle();
                }
                if (tab == excludedTab && extraExclTab != null) {
                    final Rect r = new Rect(rect);
                    r.offset(-excludedTab.left - extraExclTab.left, -excludedTab.top - extraExclTab.top);
                    canvas.drawBitmap(extraExclTab.bitmap, r, relative, PAINT_SRC_OVER);
                }
            }
        } catch (RuntimeException e) {
            bitmap.recycle();
            throw e;
        }

        return bitmap;
    }

    public void setLevel(@IntRange(from = 0) int level) {
        if (level < 0) {
            return;
        }
        this.level = level;
    }

    public void showLayerLevel() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; ++i) {
            sb.append('→');
        }
        tvLayerLevel.setText(sb);
    }
}
