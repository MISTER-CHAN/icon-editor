package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.misterchan.iconeditor.ui.LayerAdapter;

import java.util.ArrayList;
import java.util.List;

public class Frame {
    private static final Paint PAINT_SRC = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    private Bitmap thumbnail;
    public int delay;
    public int selectedLayerIndex = 0;
    public LayerTree layerTree;
    public final List<Layer> layers = new ArrayList<>();
    public final LayerAdapter layerAdapter = new LayerAdapter(this);

    public void computeLayerTree() {
        layerTree = Layers.computeLayerTree(layers);
    }

    public synchronized void createThumbnail() {
        if (thumbnail != null && !thumbnail.isRecycled()) {
            thumbnail.recycle();
        }
        thumbnail = Layers.mergeLayers(layerTree);
    }

    public Layer getBackgroundLayer() {
        return layers.get(layers.size() - 1);
    }

    public Layer getSelectedLayer() {
        return layers.get(selectedLayerIndex);
    }

    public synchronized Bitmap getThumbnail() {
        if (thumbnail == null || thumbnail.isRecycled()) {
            createThumbnail();
        }
        return thumbnail;
    }

    public int group() {
        int bottomPos = layers.size() - 1;
        for (int i = selectedLayerIndex; i <= bottomPos; ++i) {
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
        final List<Layer> refLayers = new ArrayList<>();
        for (int i = layers.size() - 1; i >= 0; --i) {
            final Layer layer = layers.get(i);
            if (layer.reference && i != selectedLayerIndex) {
                refLayers.add(0, layer);
            }
        }
        switch (refLayers.size()) {
            case 0 -> {
                return null;
            }
            case 1 -> {
                final Layer layer = refLayers.get(0);
                final Bitmap src = layer.bitmap, background = getBackgroundLayer().bitmap;
                final Bitmap dst = Bitmap.createBitmap(background.getWidth(), background.getHeight(), Bitmap.Config.ARGB_8888);
                new Canvas(dst).drawBitmap(src, layer.left, layer.top, PAINT_SRC);
                return dst;
            }
        }
        final Bitmap background = getBackgroundLayer().bitmap;
        return Layers.mergeLayers(Layers.computeLayerTree(refLayers),
                new Rect(0, 0, background.getWidth(), background.getHeight()), false);
    }

    public synchronized void recycleThumbnail() {
        if (thumbnail != null) {
            if (!thumbnail.isRecycled()) thumbnail.recycle();
            thumbnail = null;
        }
    }
}
