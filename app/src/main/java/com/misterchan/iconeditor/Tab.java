package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.annotation.StringRes;

import com.waynejo.androidndkgif.GifEncoder;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

class Tab {
    public enum FileType {
        PNG, JPEG, GIF, WEBP
    }

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

    public boolean drawBelow = false;
    public boolean gifDither = true; // Ignored if not background
    public boolean isBackground = true;
    public boolean isFirstFrame = true; // Ignored if not background
    public boolean visible = true;
    public Bitmap bitmap;
    public Bitmap.CompressFormat compressFormat; // Ignored if not background
    public final CellGrid cellGrid = new CellGrid();
    private CheckBox cbVisible;
    public final Deque<Guide> guides = new LinkedList<>();
    public GifEncoder.EncodingType gifEncodingType; // Ignored if not background
    public FileType fileType; // Ignored if not background
    public Filter filter;
    public float scale;
    public float translationX, translationY;
    public final History history = new History();
    private int backgroundPosition;
    public int delay; // Ignored if not background
    private int level = 0;
    public int left = 0, top = 0;
    public int quality = -1; // Ignored if not background
    public LayerTree layerTree; // Ignored if not background
    public String filePath; // Ignored if not background
    private Tab background;
    private Tab firstFrame; // Ignored if not background
    private TextView tvBackground;
    private TextView tvLowerLevel, tvRoot, tvParent, tvLeaf;
    private TextView tvTitle;

    @Size(20)
    public float[] colorMatrix = {
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    @Size(3)
    public float[] deltaHsv = {0.0f, 0.0f, 0.0f};

    @Size(5)
    public int[][] curves = {new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100]};

    public final Paint paint = new Paint() {
        {
            setAntiAlias(false);
            setFilterBitmap(false);
        }
    };

    {
        for (int i = 0; i <= 4; ++i) {
            for (int j = 0x0; j < 0x100; ++j) {
                curves[i][j] = j;
            }
        }
    }

    private static void addFilters(Bitmap bitmap, Tab tab) {
        switch (tab.filter) {
            case COLOR_MATRIX -> BitmapUtil.addColorMatrixColorFilter(
                    bitmap, 0, 0, bitmap, 0, 0, tab.colorMatrix);
            case CURVES -> BitmapUtil.applyCurves(bitmap, tab.curves);
            case HSV -> BitmapUtil.shiftHsv(bitmap, tab.deltaHsv);
        }
    }

    private void clearLevelIcons() {
        tvLeaf.setText("");
        tvLowerLevel.setText("");
        tvParent.setText("");
        tvRoot.setText("");
    }

    public static LayerTree computeLayerTree(List<Tab> tabs, Tab tab) {
        final Stack<LayerTree> stack = new Stack<>();
        LayerTree layerTree = new LayerTree();
        LayerTree.Node prev = null;

        final Tab background = tab.getBackground();
        stack.push(layerTree);
        for (int i = background.getBackgroundPosition(); i >= 0; --i) {
            final Tab t = tabs.get(i);
            if (t.isBackground) {
                if (t == background) {
                    prev = layerTree.push(t);
                    continue;
                } else {
                    break;
                }
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
                prev = lt.push(t);

            } else /* if (levelDiff < 0) */ {
                // If current level is lower than or equal to background's level
                if (-levelDiff < stack.size()) {
                    for (int j = 0; j > levelDiff; --j) {
                        stack.pop();
                    }
                    prev = stack.peek().push(t);
                } else {
                    // Re-compute layer tree
                    stack.clear();
                    layerTree = stack.push(new LayerTree());
                    prev = layerTree.push(t);
                }

            }
        }

        return layerTree;
    }

    public static void distinguishProjects(List<Tab> tabs) {
        final int last = tabs.size() - 1;
        Tab background = tabs.get(last);
        int backgroundPos = last;
        background.isBackground = true;
        for (int i = last; i >= 0; --i) {
            final Tab tab = tabs.get(i);
            if (tab.isBackground) {
                background = tab;
                backgroundPos = i;
            }
            tab.background = background;
            tab.backgroundPosition = backgroundPos;
        }

        Tab firstFrame = null;
        for (int i = 0; i < tabs.size(); ++i) {
            final Tab tab = tabs.get(i).getBackground();
            i = tab.getBackgroundPosition();
            if (firstFrame == null) {
                tab.isFirstFrame = true;
            }
            if (tab.isFirstFrame) {
                firstFrame = tab;
            }
            tab.firstFrame = firstFrame;
        }
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

    public Tab getFirstFrame() {
        return firstFrame;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        final String s = tvTitle.getText().toString();
        final int i = s.lastIndexOf('.');
        return i == -1 ? s : s.substring(0, i);
    }

    public CharSequence getTitle() {
        return tvTitle != null ? tvTitle.getText() : "";
    }

    public void inheritPropertiesFrom(Tab background) {
        delay = background.delay;
        compressFormat = background.compressFormat;
        filePath = background.filePath;
        fileType = background.fileType;
        gifDither = background.gifDither;
        gifEncodingType = background.gifEncodingType;
        isBackground = true;
        isFirstFrame = background.isFirstFrame;
        layerTree = background.layerTree;
        quality = background.quality;
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

    public static Bitmap mergeLayers(final LayerTree tree) {
        final Bitmap background = tree.getBackground().getTab().bitmap;
        return mergeLayers(tree, new Rect(0, 0, background.getWidth(), background.getHeight()),
                null, null, null);
    }

    public static Bitmap mergeLayers(final LayerTree tree, final Rect rect,
                                     final Tab specialTab, final Bitmap bmOfSpecialTab, final Tab extraTab) {
        return mergeLayers(tree, rect, null, specialTab, bmOfSpecialTab, extraTab);
    }

    /**
     * @param specialTab     The layer whose bitmap is going to be replaced
     * @param bmOfSpecialTab The bitmap to replace with
     * @param extraTab       The extra layer to draw over the special layer
     */
    public static Bitmap mergeLayers(final LayerTree tree, final Rect rect, final Bitmap background,
                                     final Tab specialTab, final Bitmap bmOfSpecialTab, final Tab extraTab) {
        final LayerTree.Node backgroundNode = tree.getBackground();
        final Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        if (background != null) {
            canvas.drawBitmap(background, 0.0f, 0.0f, PAINT_SRC);
        }

        try {
            for (LayerTree.Node node = backgroundNode; node != null; node = node.getAbove()) {
                final Tab tab = node.getTab();
                if (!tab.visible) {
                    continue;
                }
                final LayerTree children = node.getChildren();
                if (children == null) {
                    final Paint paint = node == backgroundNode && background == null ? PAINT_SRC : tab.paint;
                    final int bmW = tab.bitmap.getWidth(), bmH = tab.bitmap.getHeight();
                    // Rectangle src and dst are intersection between background layer subset and current layer
                    final Rect src = new Rect(0, 0, bmW, bmH);
                    final int srcOrigLeft = -tab.left, srcOrigTop = -tab.top; // Origin location relative to layer
                    if (!src.intersect(srcOrigLeft + rect.left, srcOrigTop + rect.top, srcOrigLeft + rect.right, srcOrigTop + rect.bottom)) {
                        continue; // No intersection
                    }
                    final Rect dst = new Rect(0, 0, rect.width(), rect.height());
                    final int dstLeft = -rect.left + tab.left, dstTop = -rect.top + tab.top; // Layer location relative to background layer subset
                    dst.intersect(dstLeft, dstTop, dstLeft + bmW, dstTop + bmH);
                    final int intW = src.width(), intH = src.height(); // Intersection size, src size == dst size
                    final Rect intRel = new Rect(0, 0, intW, intH); // Intersection relative rectangle

                    Rect extraSrc = null, extraDst = null; // Intersection between intersection and extra layer
                    if (tab == specialTab && extraTab != null) {
                        final int w = extraTab.bitmap.getWidth(), h = extraTab.bitmap.getHeight();
                        extraSrc = new Rect(0, 0, w, h);
                        final int sol = -extraTab.left - tab.left + rect.left, sot = -extraTab.top - tab.top + rect.top; // Origin location relative to extra layer
                        if (extraSrc.intersect(sol + dst.left, sot + dst.top, sol + dst.right, sot + dst.bottom)) {
                            extraDst = new Rect(intRel);
                            final int dl = -dst.left - rect.left + tab.left + extraTab.left, dt = -dst.top - rect.top + tab.top + extraTab.top; // Extra location relative to intersection
                            extraDst.intersect(dl, dt, dl + w, dt + h);
                        }
                    }

                    if (tab.filter == null) {
                        if (tab.drawBelow) {
                            canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
                        } else if (tab == specialTab) {
                            if (extraDst != null) {
                                final Bitmap bm = Bitmap.createBitmap(intW, intH, Bitmap.Config.ARGB_8888); // Intersection bitmap
                                final Canvas cv = new Canvas(bm);
                                try {
                                    cv.drawBitmap(bmOfSpecialTab, src, intRel, PAINT_SRC);
                                    cv.drawBitmap(extraTab.bitmap, extraSrc, extraDst, extraTab.paint);
                                    canvas.drawBitmap(bm, intRel, dst, paint);
                                } finally {
                                    bm.recycle();
                                }
                            } else {
                                canvas.drawBitmap(bmOfSpecialTab, src, dst, paint);
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
                            } else if (tab == specialTab) {
                                cv.drawBitmap(bmOfSpecialTab, src, intRel, PAINT_SRC);
                                if (extraDst != null) {
                                    cv.drawBitmap(extraTab.bitmap, extraSrc, extraDst, extraTab.paint);
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
                            specialTab, bmOfSpecialTab, extraTab);
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
     * @param listener the callback to call on checked state of {@link #cbVisible} change
     */
    public void addOVCBCCListener(CompoundButton.OnCheckedChangeListener listener) {
        cbVisible.setOnCheckedChangeListener(listener);
    }

    public void removeOVCBCCListener() {
        cbVisible.setOnCheckedChangeListener(null);
    }

    public void setTitle(@StringRes int resId) {
        tvTitle.setText(resId);
    }

    public void setTitle(CharSequence text) {
        tvTitle.setText(text);
    }

    public void setVisible(boolean visible) {
        cbVisible.setChecked(visible);
    }

    private static void showBackgroundIcons(List<Tab> tabs) {
        Tab lastTab = null;
        for (final Tab tab : tabs) {
            tab.tvBackground.setText("");
            if (!tab.isBackground) {
                continue;
            }
            if (lastTab != null) {
                lastTab.tvBackground.append(tab.isFirstFrame ? "┃" : "│");
            }
            lastTab = tab;
        }
        lastTab.tvBackground.append("┃");
    }

    public static void showIcons(List<Tab> tabs, Tab tab) {
        showVisibilityIcons(tabs, tab.background);
        showBackgroundIcons(tabs);
        showLevelIcons(tabs, tab.backgroundPosition);
    }

    public static void showLevelIcons(List<Tab> tabs, int backgroundPos) {
        Tab lastTab = null;
        for (int i = backgroundPos; i >= 0; --i) {
            final Tab t = tabs.get(i);
            if (t.isBackground && i != backgroundPos) {
                break;
            }

            t.clearLevelIcons();
            if (t.level > 0) {
                t.tvLowerLevel.append("→");
            }
            if (lastTab == null) {
                if (t.level > 0) {
                    t.tvRoot.append("]".repeat(t.level - 1));
                }
            } else {
                final int levelDiff = t.level - lastTab.level;
                if (levelDiff > 0) {
                    lastTab.tvParent.append("]".repeat(lastTab.level > 0 ? levelDiff : levelDiff - 1));
                } else if (levelDiff < 0) {
                    lastTab.tvLeaf.append("[".repeat(t.level > 0 ? -levelDiff : -levelDiff - 1));
                }
            }
            lastTab = t;
        }
        if (lastTab.level > 0) {
            lastTab.tvLeaf.append("[".repeat(lastTab.level - 1));
        }
    }

    public void showTo(View view) {
        ((TextView) view.findViewById(R.id.tv_background)).setText(tvBackground.getText());
        ((TextView) view.findViewById(R.id.tv_leaf)).setText(tvLeaf.getText());
        ((TextView) view.findViewById(R.id.tv_lower_level)).setText(tvLowerLevel.getText());
        ((TextView) view.findViewById(R.id.tv_parent)).setText(tvParent.getText());
        ((TextView) view.findViewById(R.id.tv_root)).setText(tvRoot.getText());
        ((TextView) view.findViewById(R.id.tv_title)).setText(tvTitle.getText());
    }

    private static void showVisibilityIcons(List<Tab> tabs, Tab background) {
        for (int i = 0; i < tabs.size(); ++i) {
            final Tab tab = tabs.get(i);
            tab.cbVisible.setVisibility(tab.background == background ? View.VISIBLE : View.GONE);
        }
    }
}
