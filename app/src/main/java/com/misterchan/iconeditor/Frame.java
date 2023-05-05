package com.misterchan.iconeditor;

import java.util.ArrayList;
import java.util.List;

public class Frame {


    public int delay;
    public Layer selectedLayer;
    public LayerTree layerTree; // Ignored if not background
    public final List<Layer> layers = new ArrayList<>();
    public final LayerAdapter layerAdapter = new LayerAdapter(this);
}
