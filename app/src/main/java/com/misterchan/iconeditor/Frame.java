package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Frame {
    private static final Paint PAINT_SRC = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    public int delay;
    public int selectedLayerIndex = 0;
    public LayerTree layerTree;
    public final List<Layer> layers = new ArrayList<>();
    public final LayerAdapter layerAdapter = new LayerAdapter(this);

    public void computeLayerTree() {
        layerTree = Layers.computeLayerTree(layers);
    }

    public Layer getBackgroundLayer() {
        return layers.get(layers.size() - 1);
    }

    public int group() {
        int bottomPos = layers.size() - 1;
        for (int i = selectedLayerIndex; i < bottomPos; ++i) {
            final Layer layer = layers.get(i);
            if (!layer.visible) {
                bottomPos = i - 1;
                break;
            }
        }
        for (int i = bottomPos; i >= 0; --i) {
            final Layer layer = layers.get(i);
            if (!layer.visible) {
                break;
            }
            layer.levelDown();
        }
        return bottomPos + 1;
    }

    public Bitmap mergeReferenceLayers() {
        final List<Integer> refLayersIndexes = new ArrayList<>();
        for (int i = layers.size() - 1; i >= 0; --i) {
            final Layer layer = layers.get(i);
            if (layer.reference && i != selectedLayerIndex) {
                refLayersIndexes.add(i);
            }
        }
        switch (refLayersIndexes.size()) {
            case 0 -> {
                return null;
            }
            case 1 -> {
                final Bitmap src = layers.get(refLayersIndexes.get(0)).bitmap;
                final Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
                new Canvas(dst).drawBitmap(src, 0.0f, 0.0f, PAINT_SRC);
                return dst;
            }
        }
        return Layers.mergeLayers(Layers.computeLayerTree(layers, refLayersIndexes));
    }
}
