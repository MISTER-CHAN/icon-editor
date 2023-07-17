package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.misterchan.iconeditor.util.BitmapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Layers {
    private static final Paint PAINT_SRC = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    private static final Paint PAINT_SRC_OVER = new Paint() {
        {
            setAntiAlias(false);
            setFilterBitmap(false);
        }
    };

    private static void addFilters(Bitmap bitmap, Layer layer) {
        addFilters(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), layer);
    }

    private static void addFilters(Bitmap bitmap, Rect rect, Layer layer) {
        if (layer.filter == null) {
            return;
        }
        switch (layer.filter) {
            case COLOR_BALANCE, CONTRAST, LEVELS, LIGHTING, LIGHTNESS ->
                    BitmapUtils.addLightingColorFilter(bitmap, rect, layer.lighting);
            case COLOR_MATRIX, SATURATION, THRESHOLD ->
                    BitmapUtils.addColorMatrixColorFilter(bitmap, rect, layer.colorMatrix.getArray());
            case CURVES -> BitmapUtils.applyCurves(bitmap, rect, layer.curves);
            case HSV -> BitmapUtils.shiftHsv(bitmap, rect, layer.deltaHsv);
        }
    }

    public static LayerTree computeLayerTree(List<Layer> layers) {
        final List<Integer> indices = new ArrayList<>();
        for (int i = layers.size() - 1; i >= 0; --i) {
            indices.add(i);
        }
        return computeLayerTree(layers, indices);
    }

    /**
     * @param layers  Layer list starts with foreground
     * @param indices Layer index list starts with background
     */
    public static LayerTree computeLayerTree(List<Layer> layers, List<Integer> indices) {
        final Stack<LayerTree> stack = new Stack<>();
        LayerTree layerTree = new LayerTree();
        LayerTree.Node prev = layerTree.push(layers.get(indices.get(0)), true);

        stack.push(layerTree);
        for (int i = 1; i < indices.size(); ++i) {
            final Layer l = layers.get(indices.get(i));

            final Layer prevLayer = prev.layer;
            final int levelDiff = l.getLevel() - prevLayer.getLevel();

            if (levelDiff == 0) {
                prev = stack.peek().push(l);

            } else if (levelDiff > 0) {
                LayerTree lt = null;
                for (int j = 0; j < levelDiff; ++j) {
                    lt = new LayerTree();
                    prev.children = lt;
                    prev = lt.push(prevLayer);
                    stack.push(lt);
                }
                prev = lt.push(l);

            } else /* if (levelDiff < 0) */ {
                // If current level is lower than or equal to background's level
                if (-levelDiff < stack.size()) {
                    for (int j = 0; j > levelDiff; --j) {
                        stack.pop();
                    }
                    prev = stack.peek().push(l);
                } else {
                    // Re-compute layer tree
                    stack.clear();
                    layerTree = stack.push(new LayerTree());
                    prev = layerTree.push(l);
                }

            }
        }

        return layerTree;
    }

    public static void levelDown(List<Layer> layers, int position) {
        final Layer layer = layers.get(position);
        final int level = layer.getLevel();
        layer.levelDown();
        for (int i = position - 1; i >= 0; --i) {
            final Layer t = layers.get(i);
            if (t.getLevel() > level) {
                t.levelDown();
            } else {
                break;
            }
        }
    }

    public static void mergeLayers(Layer top, Layer bottom) {
        final Bitmap uBm = Bitmap.createBitmap(top.bitmap.getWidth(), top.bitmap.getHeight(),
                top.bitmap.getConfig(), top.bitmap.hasAlpha(), top.bitmap.getColorSpace());
        final Bitmap lBm = bottom.bitmap;
        final Canvas uCv = new Canvas(uBm), lCv = new Canvas(lBm);
        uCv.drawBitmap(top.passBelow ? lBm : top.bitmap, 0.0f, 0.0f, PAINT_SRC);
        if (top.filter != null) {
            addFilters(uBm, top);
        }
        lCv.drawBitmap(uBm, top.left - bottom.left, top.top - bottom.top, top.paint);
        uBm.recycle();
    }

    public static Bitmap mergeLayers(final LayerTree tree) {
        final Bitmap background = tree.getBackground().layer.bitmap;
        return mergeLayers(tree, new Rect(0, 0, background.getWidth(), background.getHeight()),
                null, null, null);
    }

    public static Bitmap mergeLayers(final LayerTree tree, final Rect rect,
                                     final Layer specifiedLayer, final Bitmap specifiedLayerBm, final FloatingLayer extraLayer) {
        return mergeLayers(tree, rect, null, null, specifiedLayer, specifiedLayerBm, extraLayer);
    }

    /**
     * @param specifiedLayer   The layer whose bitmap is going to be replaced
     * @param specifiedLayerBm The bitmap to replace with
     * @param extraLayer       The extra layer to draw over the special layer
     * @throws RuntimeException if any bitmap being drawn is recycled as this method is not thread-safe
     */
    public static Bitmap mergeLayers(final LayerTree tree, final Rect rect,
                                     final Bitmap base, final Paint basePaint,
                                     final Layer specifiedLayer, final Bitmap specifiedLayerBm, final FloatingLayer extraLayer) throws RuntimeException {
        final LayerTree.Node backgroundNode = tree.getBackground();
        final Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        if (base != null) {
            canvas.drawBitmap(base, 0.0f, 0.0f, basePaint);
        }

        try {
            for (LayerTree.Node node = backgroundNode; node != null; node = node.getAbove()) {
                final Layer layer = node.layer;
                if (!layer.visible) {
                    continue;
                }
                final LayerTree children = node.children;
                int[] pixels = null;
                if (children == null) {
                    final int bmW = layer.bitmap.getWidth(), bmH = layer.bitmap.getHeight();
                    // Rectangle src and dst are intersection between background layer subset and current layer
                    final Rect src = new Rect(0, 0, bmW, bmH);
                    final int srcOrigLeft = -layer.left, srcOrigTop = -layer.top; // Origin location relative to layer
                    if (!src.intersect(srcOrigLeft + rect.left, srcOrigTop + rect.top, srcOrigLeft + rect.right, srcOrigTop + rect.bottom)) {
                        continue; // No intersection
                    }
                    final Rect dst = new Rect(0, 0, rect.width(), rect.height());
                    final int dstLeft = -rect.left + layer.left, dstTop = -rect.top + layer.top; // Layer location relative to background layer subset
                    dst.intersect(dstLeft, dstTop, dstLeft + bmW, dstTop + bmH);
                    final int intW = src.width(), intH = src.height(); // Intersection size, src size == dst size
                    final Rect intRel = new Rect(0, 0, intW, intH); // Intersection relative rectangle

                    Rect extraSrc = null, extraDst = null; // Intersection between intersection and extra layer
                    if (layer == specifiedLayer && extraLayer != null) {
                        if (extraLayer.hasRect()) {
                            final int w = extraLayer.getWidth(), h = extraLayer.getHeight();
                            extraSrc = new Rect(0, 0, w, h);
                            final int sol = -extraLayer.getLeft() - layer.left + rect.left, sot = -extraLayer.getTop() - layer.top + rect.top; // Origin location relative to extra layer
                            if (extraSrc.intersect(sol + dst.left, sot + dst.top, sol + dst.right, sot + dst.bottom)) {
                                extraDst = new Rect(intRel);
                                final int dl = -dst.left - rect.left + layer.left + extraLayer.getLeft(), dt = -dst.top - rect.top + layer.top + extraLayer.getTop(); // Extra location relative to intersection
                                extraDst.intersect(dl, dt, dl + w, dt + h);
                            }
                        } else {
                            extraSrc = src;
                            extraDst = intRel;
                        }
                    }

                    final Paint paint = node != backgroundNode || node.isRoot || base != null ? layer.paint : PAINT_SRC;
                    if (layer.clipToBelow) {
                        pixels = BitmapUtils.getPixels(bitmap, dst);
                    }
                    if (layer == specifiedLayer) {
                        if (extraDst != null) {
                            final Bitmap bmExtra = Bitmap.createBitmap(intW, intH, Bitmap.Config.ARGB_8888); // Intersection bitmap
                            final Canvas cvExtra = new Canvas(bmExtra);
                            try {
                                cvExtra.drawBitmap(specifiedLayerBm, src, intRel, PAINT_SRC);
                                cvExtra.drawBitmap(extraLayer.getBitmap(), extraSrc, extraDst, PAINT_SRC_OVER);
                                canvas.drawBitmap(bmExtra, intRel, dst, paint);
                            } finally {
                                bmExtra.recycle();
                            }
                        } else {
                            canvas.drawBitmap(specifiedLayerBm, src, dst, paint);
                        }
                    } else {
                        canvas.drawBitmap(layer.bitmap, src, dst, paint);
                    }
                    if (layer.colorRange.enabled) {
                        BitmapUtils.selectByColorRange(bitmap, dst, layer.colorRange);
                    }
                    if (layer.filter != null) {
                        addFilters(bitmap, dst, layer);
                    }
                    if (layer.clipToBelow) {
                        BitmapUtils.clip(bitmap, dst, pixels);
                    }
                } else {
                    final Bitmap mergedChildren;
                    if (layer.clipToBelow) {
                        pixels = BitmapUtils.getPixels(bitmap, rect);
                    }
                    if (!layer.passBelow) {
                        mergedChildren = mergeLayers(children, rect, null, null,
                                specifiedLayer, specifiedLayerBm, extraLayer);
                        canvas.drawBitmap(mergedChildren, 0.0f, 0.0f, layer.paint);
                    } else {
                        mergedChildren = mergeLayers(children, rect, bitmap, layer.paint,
                                specifiedLayer, specifiedLayerBm, extraLayer);
                        BitmapUtils.fillInBlank(mergedChildren, bitmap);
                    }
                    if (layer.clipToBelow) {
                        BitmapUtils.clip(bitmap, rect, pixels);
                    }
                    mergedChildren.recycle();
                }
            }
        } catch (RuntimeException e) {
            bitmap.recycle();
            throw e;
        }

        return bitmap;
    }
}
